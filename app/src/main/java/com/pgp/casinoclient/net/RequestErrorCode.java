package com.pgp.casinoclient.net;

public enum RequestErrorCode {
    GOOD,
    ERROR,
    INVALID_PACKAGE_TYPE,
    DATA_MISSING$PLAYER_ID,
    DATA_MISSING$PLAYER_PASSWORD,
    DATA_MISSING$RESERVED_1,
    DATA_MISSING$RESERVED_2,
    DATA_MISSING$RESERVED_3,
    DATA_MISSING$RESERVED_4,
    DATA_MISSING$RESERVED_5,
    DATA_MISSING$RESERVED_6,
    DATA_MISSING$RESERVED_7,
    DATA_MISSING$RESERVED_8,
    DATA_NOT_FOUND$PLAYER_WITH_ID,
    DATA_NOT_FOUND$PLAYER_WITH_PASSWORD;



    public static RequestErrorCode get(byte index) {
        switch(index) {
            case 0:
                return GOOD;
            case 1:
                return ERROR;
            case 2:
                return INVALID_PACKAGE_TYPE;
            case 3:
                return DATA_MISSING$PLAYER_ID;
            case 4:
                return DATA_MISSING$PLAYER_PASSWORD;
            case 5:
                return DATA_MISSING$RESERVED_1;
            case 6:
                return DATA_MISSING$RESERVED_1;
            case 7:
                return DATA_MISSING$RESERVED_1;
            case 8:
                return DATA_MISSING$RESERVED_1;
            case 9:
                return DATA_MISSING$RESERVED_1;
            case 10:
                return DATA_MISSING$RESERVED_1;
            case 11:
                return DATA_MISSING$RESERVED_1;
            case 12:
                return DATA_MISSING$RESERVED_1;
            case 13:
                return DATA_NOT_FOUND$PLAYER_WITH_ID;
            case 14:
                return DATA_NOT_FOUND$PLAYER_WITH_PASSWORD;
            default:
                return ERROR;
        }
    }

}
