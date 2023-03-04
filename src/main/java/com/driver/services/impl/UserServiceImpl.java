package com.driver.services.impl;

import com.driver.model.Country;
import com.driver.model.CountryName;
import com.driver.model.ServiceProvider;
import com.driver.model.User;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository3;
    @Autowired
    ServiceProviderRepository serviceProviderRepository3;
    @Autowired
    CountryRepository countryRepository3;

    @Override
    public User register(String username, String password, String countryName) throws Exception{
        User user=new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setConnected(false);

        Country country=new Country();
        country.setCountryName(CountryName.valueOf(countryName));
        country.setCode(CountryName.valueOf(countryName).toCode());

        country.setUser(user);

        user.setOriginalIp(country.getCode()+"."+user.getId());
        user.setMaskedIp(null);
        user.setOriginalCountry(country);
        user.setCurrentCountry(countryName);
        userRepository3.save(user);
        return user;

    }

    @Override
    public User subscribe(Integer userId, Integer serviceProviderId) {
        User user=userRepository3.findById(userId).get();
        ServiceProvider serviceProvider=serviceProviderRepository3.findById(serviceProviderId).get();
        serviceProvider.getCountryList().add(user.getOriginalCountry());
        serviceProvider.getUsers().add(user);
        user.getOriginalCountry().setServiceProvider(serviceProvider);
        user.getServiceProviderList().add(serviceProvider);
       serviceProviderRepository3.save(serviceProvider);
        return user;
    }
}
