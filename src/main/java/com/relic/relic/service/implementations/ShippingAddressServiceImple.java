package com.relic.relic.service.implementations;

import com.cloudinary.utils.StringUtils;
import com.relic.relic.dto.CreateShippingAddressDTO;
import com.relic.relic.dto.ProductDto;
import com.relic.relic.exception.BiddingUnauthorizedException;
import com.relic.relic.exception.CustomException;
import com.relic.relic.exception.ResourceNotFoundException;
import com.relic.relic.model.ShippingAddress;
import com.relic.relic.model.User;
import com.relic.relic.repository.ShippingAddressRepository;
import com.relic.relic.repository.UserRepository;
import com.relic.relic.service.ShippingAddressService;
import com.relic.relic.util.UserAuthenticationHelper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class ShippingAddressServiceImple implements ShippingAddressService {
    private ModelMapper modelMapper;
    private UserRepository userRepository;
    private ShippingAddressRepository shippingAddressRepository;
    private UserAuthenticationHelper userAuthenticationHelper;
    @Override
    public String saveAddress(CreateShippingAddressDTO createShippingAddressDTO) {
        if (createShippingAddressDTO == null ||
                StringUtils.isBlank(createShippingAddressDTO.getStreetAddress()) ||
                StringUtils.isBlank(createShippingAddressDTO.getCity()) ||
                StringUtils.isBlank(createShippingAddressDTO.getState()) ||
                StringUtils.isBlank(createShippingAddressDTO.getLastname()) ||
                StringUtils.isBlank(createShippingAddressDTO.getFirstname()) ||
                StringUtils.isBlank(createShippingAddressDTO.getCountry()) )
        {
            throw new CustomException("All fields are required");
        }

        User user = userAuthenticationHelper.getLoggedInUser();
        Optional<ShippingAddress> optionalShippingAddress = shippingAddressRepository.findByUser(user);

        ShippingAddress newAddress;

        newAddress = optionalShippingAddress.orElseGet(ShippingAddress::new);

        newAddress.setStreetAddress(createShippingAddressDTO.getStreetAddress());
        newAddress.setCity(createShippingAddressDTO.getCity());
        newAddress.setCountry(createShippingAddressDTO.getCountry());
        newAddress.setState(createShippingAddressDTO.getState());
        if(!optionalShippingAddress.isPresent()){
            newAddress.setUser(user);
        }
        newAddress.setLastname(createShippingAddressDTO.getLastname());
        newAddress.setFirstname(createShippingAddressDTO.getFirstname());
        shippingAddressRepository.save(newAddress);

        return "Shipping address saved succesfully";
    }

    @Override
    public CreateShippingAddressDTO getUserShippingAddress() {
        User user = userAuthenticationHelper.getLoggedInUser();
        Optional<ShippingAddress> shippingAddress = shippingAddressRepository.findByUser(user);

        return modelMapper.map(shippingAddress.get(), CreateShippingAddressDTO.class);
    }
}
