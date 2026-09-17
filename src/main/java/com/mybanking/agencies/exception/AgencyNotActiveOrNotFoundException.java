package com.mybanking.agencies.exception;

import com.mybanking.agencies.domain.http.RegisterSituationEnum;

public class AgencyNotActiveOrNotFoundException extends RuntimeException {

    @Override
    public String getMessage() {
        return "The agency status is " + RegisterSituationEnum.INACTIVE + " or not found.";
    }
}
