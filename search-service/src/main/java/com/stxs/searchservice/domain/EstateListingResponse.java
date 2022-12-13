package com.stxs.searchservice.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

@EqualsAndHashCode(callSuper = true)
@Data
public class EstateListingResponse extends EstateListing {
    private Double score;
    public EstateListingResponse(EstateListing estateListing) {
        BeanUtils.copyProperties(estateListing, this);
    }
}
