
import com.securityapp.gofundme.model.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Handy
 */
// DonationRequest.java
@Data
public class DonationRequest {
    @NotNull
    private Long campaignId;
    
    @NotNull
    @DecimalMin("1.00")
    private BigDecimal amount;
    
    @NotNull
    private PaymentMethod method;
    
    private String message;
    private boolean anonymous;
    
}