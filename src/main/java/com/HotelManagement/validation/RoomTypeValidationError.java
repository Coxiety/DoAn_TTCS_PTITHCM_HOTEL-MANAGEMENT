package com.HotelManagement.validation;

public enum RoomTypeValidationError {
    DUPLICATE_ROOM_TYPE("Trường bắt buộc và phải là duy nhất"),
    INVALID_ROOM_CAPACITY("Giá trị phải lớn hơn 0"),
    INVALID_ROOM_PRICE("Giá trị phải lớn hơn 0"),
    EMPTY_ROOM_DESCRIPTION("Trường bắt buộc, không được để trống"),
    EMPTY_AMENITIES("Phải có ít nhất 1 tiện nghi"),
    INVALID_IMAGE_FORMAT("Định dạng tệp ảnh chỉ chấp nhận JPG hoặc PNG");

    private final String message;

    RoomTypeValidationError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
} 