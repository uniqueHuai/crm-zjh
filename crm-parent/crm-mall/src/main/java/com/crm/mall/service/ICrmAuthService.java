package com.crm.mall.service;

public interface ICrmAuthService {

    Long findOrCreateCustomer(String openid, String unionid);

    void saveSession(Long customerId, String openid, String unionid, String token);
}
