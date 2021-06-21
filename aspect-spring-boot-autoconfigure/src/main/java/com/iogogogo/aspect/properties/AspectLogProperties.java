package com.iogogogo.aspect.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by tao.zeng on 2021/6/21.
 */
@Data
@ConfigurationProperties(prefix = "aspect")
public class AspectLogProperties {

    private boolean enable;

}
