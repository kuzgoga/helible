package com.helible.pilot.dataclasses

enum class DeviceStatus {
    Idle,
    IsPreparingForTakeoff,
    IsFlying,
    IsBoarding,
    IsImuCalibration,
    ChargeRequired;

    fun description(): String {
        return when (this) {
            Idle -> "Готово к работе"
            IsPreparingForTakeoff -> "Подготовка к полёту"
            IsFlying -> "В полёте"
            IsBoarding -> "Посадка"
            IsImuCalibration -> "Калибровка..."
            ChargeRequired -> "Аккумулятор разряжен"
        }
    }
}