package com.taoshao.companionspace.model.request;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author taoshao
 * @Date 2025年1月17日
 */
@Data
public class UserGeoRequest implements Serializable {
    private static final long serialVersionUID = 5579195046213219475L;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

}
