package com.mybanking.agencies.domain.http;

public class AgencyHttp {

    public AgencyHttp(String name, String companyName, String cnpj, String registerSituation) {
        this.name = name;
        this.companyName = companyName;
        this.cnpj = cnpj;
        this.registerSituation = RegisterSituationEnum.valueOf(registerSituation);
    }

    private final String name;
    private final String companyName;
    private final String cnpj;
    private final RegisterSituationEnum registerSituation;

    public String getName() {
        return name;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCnpj() {
        return cnpj;
    }

    public RegisterSituationEnum getRegisterSituation() {
        return registerSituation;
    }
}

