package com.pgp.casinoclient.net;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pgp.casinoclient.utils.BinaryUtils;
import com.pgp.casinoclient.utils.PositionInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class RequestHeader {

    public Map<RequestHeaderValues, Object> Values = new HashMap<RequestHeaderValues, Object>(0);



    @NonNull
    public static RequestHeader Sample(PackageType type){
        RequestHeader r = new RequestHeader();
        r.Values.put(RequestHeaderValues.REQUEST_TYPE, RequestType.GET);
        r.Values.put(RequestHeaderValues.PACKAGE_TYPE, type);

        return r;
    }



    // Если isRequest в true => будем проверять как будто запрос исходит ОТ НАС, если false, то как будто бы
    // нам пришёл ответ на запрос
    public boolean check(boolean isRequest){
        if (isRequest){
            if (!(Values.containsKey(RequestHeaderValues.PACKAGE_TYPE) && Values.containsKey(RequestHeaderValues.REQUEST_TYPE))) {
                return false;
            }
            if (!(Values.get(RequestHeaderValues.PACKAGE_TYPE) != PackageType.INVALID && Values.get(RequestHeaderValues.REQUEST_TYPE) != RequestType.INVALID)){
                return false;
            }
        }else{
            if (!(Values.containsKey(RequestHeaderValues.ERROR_CODE))) {
                return false;
            }
        }

        return true;
    }

    @Nullable
    public static RequestHeader /*Map.Entry<RequestHeader, Integer>*/ parse(@NonNull PositionInputStream input/*, int available*/) throws IOException {
        if (input.available() >= 3){
            RequestHeader header = new RequestHeader();
            byte valuesCount = (byte)input.read();
            //available--;
            for (byte i = 0; i < valuesCount; i++){
                byte pairType = (byte)input.read();
                //available--;

                RequestHeaderValues key = RequestHeaderValues.get(pairType);

                switch (key){
                    case INVALID:
                        header.Values.put(key, null);
                        break;
                    case REQUEST_TYPE:
                        header.Values.put(key, RequestType.get((byte)input.read()));
                        //available--;
                        break;
                    case PACKAGE_TYPE:
                        header.Values.put(key, PackageType.get((byte)input.read()));
                        //available--;
                        break;
                    case PLAYER_ID:
                        header.Values.put(key, BinaryUtils.ReadInt(input));
                        //available-=4;
                        break;
                    case PLAYER_PASSWORD:
                        header.Values.put(key, BinaryUtils.ReadInt(input));
                        //available-=4;
                        break;
                    case ERROR_CODE:
                        header.Values.put(key, RequestErrorCode.get((byte)input.read()));
                        break;
                }

            }

            //return new AbstractMap.SimpleEntry<>(header, available);
            return header;
        }

        return null;
    }

    public byte[] write() throws IOException{
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        out.write(Values.size());

        for (Map.Entry<RequestHeaderValues, Object> pair : Values.entrySet()){
            out.write(pair.getKey().ordinal());
            Object val = pair.getValue();

            switch (pair.getKey()){
                case REQUEST_TYPE:
                    out.write(((RequestType)val).ordinal());
                    break;
                case PACKAGE_TYPE:
                    out.write(((PackageType)val).ordinal());
                    break;
                case PLAYER_ID:
                    out.write(BinaryUtils.Int2Bytes((int)val));
                    break;
                case PLAYER_PASSWORD:
                    out.write(BinaryUtils.Int2Bytes((int)val));
                    break;
                case ERROR_CODE:
                    out.write(((RequestErrorCode)val).ordinal());
                    break;
                default:
                    out.write(0);
                    break;
            }
        }

        return out.toByteArray();
    }
}
