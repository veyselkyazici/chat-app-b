package com.vky.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ErrorType {
    INTERNAL_ERROR(2000, "Internal Server Error", INTERNAL_SERVER_ERROR),
    BAD_REQUEST_ERROR(2001, "Invalid Parameter Error", BAD_REQUEST),
    LOGIN_ERROR_WRONG(1000,"Kullanıcı adı yada şifre hatalı",INTERNAL_SERVER_ERROR),
    LOGIN_ERROR_REQUIRED_PASSWORD(1001,"Şifre zorunlulukları, geçerli bir şifre giriniz",INTERNAL_SERVER_ERROR),
    LOGIN_ERROR_USERNAME_ERROR(1002,"Geçerli bir kullanıcı adı giriniz. ",INTERNAL_SERVER_ERROR),
    LOGIN_ERROR_USERNAME_DUPLICATE(1003,"Bu Kullanıcı adı zaten kullanılıyor.",INTERNAL_SERVER_ERROR),


    EMAIL_ALLREADY_EXISTS_ERROR(1004, "Email adresi zaten kullanılıyor", INTERNAL_SERVER_ERROR),
    EMAIL_NOT_FOUND(1005, "Email adresi bulunamadı", INTERNAL_SERVER_ERROR),
    EMAIL_ALLREADY_EXISTS_ERROR_VERIFIY(1006, "Bu email adresi ile zaten kayit işlemi yapılmış. Lütfen mailinize gelen linkten onaylama işlemini yapınız.", INTERNAL_SERVER_ERROR),
    USER_DOES_NOT_EXIST(1007, "Kullanıcı mevcut değil", INTERNAL_SERVER_ERROR),
    Email_Confirmation_Not_Completed(1008, "Email onaylaması gerçekleştirilmemiş.", INTERNAL_SERVER_ERROR);
    private int code;
    private String message;
    HttpStatus httpStatus;
}
