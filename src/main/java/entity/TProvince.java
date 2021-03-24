package entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author HAYABUSA_7
 * @since 2020-05-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TProvince implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String name;

    private String code;

    private String pcode;

    private String fieldKey;

    private Integer districtId;
}
