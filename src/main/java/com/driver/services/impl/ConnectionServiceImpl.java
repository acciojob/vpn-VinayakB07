package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    UserRepository userRepository2;
    @Autowired
    ServiceProviderRepository serviceProviderRepository2;
    @Autowired
    ConnectionRepository connectionRepository2;

    @Override
    public User connect(int userId, String countryName) throws Exception{
    User user=userRepository2.findById(userId).get();
    Country country=new Country();
    country.setCountryName(CountryName.valueOf(countryName));
    country.setCode(CountryName.valueOf(countryName).toCode());
    user.setMaskedIp(country.getCode());
    user.setConnected(true);
    userRepository2.save(user);
    return user;
    }
    @Override
    public User disconnect(int userId) throws Exception {
    User user=new User();
    user.setConnected(false);
    user.setMaskedIp(user.getOriginalIp());
    userRepository2.save(user);
    return user;
    }
    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
    User sender=userRepository2.findById(senderId).get();
    User receiver=userRepository2.findById(receiverId).get();
    Connection connection=new Connection();
    connection.setUser(receiver);
    connection.setServiceProvider(receiver.getCountry().getServiceProvider());
    sender.getCountry().getServiceProvider().getConnectionList().add(connection);
    sender.getConnectionList().add(connection);
    userRepository2.save(sender);
    return sender;
    }
}
