package com.mybanking.agencies.exception;

import com.mybanking.agencies.domain.http.RegisterSituationEnum;

public class AgencyNotActiveOrNotFoundException extends RuntimeException {

    @Override
    public String getMessage() {
        return "O status da agência é " + RegisterSituationEnum.INACTIVE + " ou não foi encontrada";
    }
}
