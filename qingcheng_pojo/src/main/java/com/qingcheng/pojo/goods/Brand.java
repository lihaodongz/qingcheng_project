package com.qingcheng.pojo.goods;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name = "tb_brand")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Brand implements Serializable {
    @Id
    private Integer id;
    private String name;
    private String image;
    private String letter;
    private Integer seq;

}
