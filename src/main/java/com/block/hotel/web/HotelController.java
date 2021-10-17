package com.block.hotel.web;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.block.hotel.constants.MqConstants;
import com.block.hotel.pojo.Hotel;
import com.block.hotel.pojo.PageResult;
import com.block.hotel.service.IHotelService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.InvalidParameterException;

@RestController
@RequestMapping("hotel")
public class HotelController {

    @Autowired
    private IHotelService hotelService;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @GetMapping("/{id}")
    public Hotel queryById(@PathVariable("id") Long id){
        return hotelService.getById(id);
    }

    @GetMapping("/list")
    public PageResult hotelList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "1") Integer size
    ){
        Page<Hotel> result = hotelService.page(new Page<>(page, size));

        return new PageResult(result.getTotal(), result.getRecords());
    }

    /**
     * 新增酒店信息后，需要发送消息给RabbitMq,
     * elasticsearch-study作为消费者去监听mq，消费消息
     * @param hotel
     */
    @PostMapping
    public void saveHotel(@RequestBody Hotel hotel){
        hotelService.save(hotel);
        //rabbitmq的消息是存在内存的，所以发送到mq的消息尽量要小
        rabbitTemplate.convertAndSend(MqConstants.HOTEL_EXCHANGE,MqConstants.HOTEL_INSERT_KEY,hotel.getId());
    }

    /**
     * 修改和新增，消息都是发给一个队列，因为Elasticsearch的新增和修改是一个方法
     * @param hotel
     */
    @PutMapping()
    public void updateById(@RequestBody Hotel hotel){
        if (hotel.getId() == null) {
            throw new InvalidParameterException("id不能为空");
        }
        hotelService.updateById(hotel);
        rabbitTemplate.convertAndSend(MqConstants.HOTEL_EXCHANGE,MqConstants.HOTEL_INSERT_KEY,hotel.getId());
    }

    /**
     * 删除
     * @param id
     */
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable("id") Long id) {
        hotelService.removeById(id);
        rabbitTemplate.convertAndSend(MqConstants.HOTEL_EXCHANGE,MqConstants.HOTEL_DELETE_KEY,id);
    }
}
