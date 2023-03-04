package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

        if(user.getConnected()){
            throw new Exception("Already connected");
        }
        if(user.getOriginalCountry().getCountryName().equals(countryName)){
            return user;
        }
        Country country=new Country();
        country.setCountryName(CountryName.valueOf(countryName));
        country.setCode(CountryName.valueOf(countryName).toCode());
        List<ServiceProvider>list=new ArrayList<>();
        try {
          list=user.getServiceProviderList();
        }catch (Exception e){
            throw new Exception("Unable to connect");
        }
        for(ServiceProvider serviceProvider:list){
            if(!serviceProvider.getCountryList().contains(countryName)){
                throw new Exception("Unable to connect");
            }
        }
        int min=Integer.MIN_VALUE;
        for(ServiceProvider serviceProvider:list){
            if(serviceProvider.getConnectionList().contains(countryName)) {
                if (serviceProvider.getId() < min) {
                    min = serviceProvider.getId();
                }
            }
        }

        ServiceProvider serviceProvider=serviceProviderRepository2.findById(min).get();

        Connection connection=new Connection();
        connection.setServiceProvider(serviceProvider);
        connection.setUser(user);

        user.setCurrentCountry(countryName);
        user.setConnected(true);
        user.setMaskedIp(country.getCode()+"."+serviceProvider.getId()+"."+user.getId());
        user.getConnectionList().add(connection);

        serviceProvider.getConnectionList().add(connection);

        userRepository2.save(user);

        return user;
    }
    @Override
    public User disconnect(int userId) throws Exception {
    User user=new User();
    if(user.getConnected()==false){
        throw new Exception("Already disconnected");
    }
    user.setCurrentCountry(user.getOriginalCountry().getCountryName().toString());
    user.setConnected(false);
    user.setMaskedIp(null);
    userRepository2.save(user);
    return user;
    }
    @Override
    public User communicate(int senderId, int receiverId) throws Exception {

        User sender = userRepository2.findById(senderId).get();
        User receiver = userRepository2.findById(receiverId).get();

        if (sender.getCurrentCountry().equals(receiver.getCurrentCountry())) {
            return sender;
        }
        if (sender.getConnected()) {
            User user = disconnect(senderId);
            user = connect(senderId, receiver.getCurrentCountry());
            return user;
        }

        User user = connect(senderId, receiver.getCurrentCountry());
        return user;

    }

}
