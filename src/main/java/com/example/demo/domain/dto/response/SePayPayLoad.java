package com.example.demo.domain.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SePayPayLoad {

    private long id; // ID giao dịch trên SePay
    private String gateWay; // Brand name của ngân hàng
    private String transactionDate; // Thời gian xảy ra giao dịch phía ngân hàng
    private String accountNumber; // Số tài khoản ngân hàng
    private String code; // Mã code thanh toán (sepay tự nhận diện dựa vào cấu hình tại Công ty -> Cấu
                         // hình chung)
    private String content; // Nội dung chuyển khoản
    private String transferType; // Loại giao dịch. in là tiền vào, out là tiền ra
    private double transferAmount; // Số tiền giao dịch
    private double accumulated; // Số dư tài khoản (lũy kế)
    private String subAccount; // Tài khoản ngân hàng phụ (tài khoản định danh)
    private String referenceCode; // Mã tham chiếu của tin nhắn sms
    private String description; // Toàn bộ nội dung tin nhắn sms

}
