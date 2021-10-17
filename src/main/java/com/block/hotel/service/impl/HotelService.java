package com.block.hotel.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.block.hotel.mapper.HotelMapper;
import com.block.hotel.pojo.Hotel;
import com.block.hotel.service.IHotelService;
import org.springframework.stereotype.Service;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {
}
