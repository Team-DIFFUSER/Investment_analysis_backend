package com.defuture.stockapp.assets;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserChartResponseDTO {
	private String contYn;
    private String nextKey;

    @JsonProperty("daly_prsm_dpst_aset_amt_prst")
    private List<DepositData> data;
    
    public UserChartResponseDTO(List<DepositData> data) {
        this.data = data;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DepositData {
        @JsonProperty("dt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
        private LocalDate date;

        @JsonProperty("prsm_dpst_aset_amt")
        private String amount;
    }
}
