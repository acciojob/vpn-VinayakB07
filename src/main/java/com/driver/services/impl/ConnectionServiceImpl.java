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
        User user = userRepository2.findById(userId).get();
        countryName = countryName.toUpperCase();
        Country country=new Country();
        country.setCountryName(CountryName.valueOf(countryName));
        country.setCode(CountryName.valueOf(countryName).toCode());
        if(user.getConnected()){
            throw new Exception("Already connected");
        }
        if(countryName.equalsIgnoreCase(user.getOriginalCountry().getCountryName().toString())){
            return  user;
        }
        ServiceProvider minIdServiceProvider=null;
        int minId =Integer.MAX_VALUE;
        String countryCode=null;
        for(ServiceProvider serviceProvider:user.getServiceProviderList()){
            for (Country country1:serviceProvider.getCountryList()){
                if(countryName.equalsIgnoreCase(country1.getCountryName().toString()) && serviceProvider.getId()<minId){
                    minIdServiceProvider=serviceProvider;
                    minId=serviceProvider.getId();
                    countryCode=country.getCode();
                }
            }

        }
        if (minIdServiceProvider==null){//no providers give connection to given country
            throw new Exception("Unable to connect");
        }

        Connection connection=new Connection();
        connection.setUser(user);
        connection.setServiceProvider(minIdServiceProvider);


        minIdServiceProvider.getConnectionList().add(connection);

        String maskedIP=countryCode+"."+minIdServiceProvider.getId()+"."+user.getId();
        user.setMaskedIp(maskedIP);
        user.setConnected(true);
        user.getConnectionList().add(connection);


        serviceProviderRepository2.save(minIdServiceProvider);


        userRepository2.save(user);

        return  user;
    }
    @Override
    public User disconnect(int userId) throws Exception {
        User user = userRepository2.findById(userId).get();
        if(!user.getConnected()){
            throw new Exception("Already disconnected");
        }
        user.setMaskedIp(null);
        user.setConnected(false);

        userRepository2.save(user);

        return user;
    }
    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
        User sender = userRepository2.findById(senderId).get();
        User reciver = userRepository2.findById(receiverId).get();

        CountryName reciverCountryName = null;
        if(reciver.getConnected()){
            String reciverCountryCode;
            String[] arr = reciver.getMaskedIp().split("\\.");
            reciverCountryCode = arr[0];
            for(CountryName countryName : CountryName.values()){
                if(countryName.toCode().equals(reciverCountryCode)){
                    reciverCountryName = countryName;
                    break;
                }
            }
        }else{
            reciverCountryName = reciver.getOriginalCountry().getCountryName();
        }

        if(reciverCountryName.equals(sender.getOriginalCountry().getCountryName())){
            return sender;
        }

        try {
            sender = connect(senderId, reciverCountryName.name());
        }catch (Exception e){
            throw new Exception("Cannot establish communication");
        }

        return sender;
    }
}