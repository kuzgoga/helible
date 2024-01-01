package com.helible.pilot.components.deviceScreen

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.helible.pilot.R

@Composable
fun defaultDeviceActionsList(): Map<String, Array<Pair<String, Pair<Pair<Int, Color>, String>>>> {
    return mapOf(
        Pair(
            "Управление",
            arrayOf(
                Pair(
                    "console",
                    Pair(
                        Pair(R.drawable.joystick, MaterialTheme.colorScheme.primary),
                        "Пульт управления"
                    )
                ),
                Pair(
                    "codeblocks",
                    Pair(
                        Pair(R.drawable.code_blocks, MaterialTheme.colorScheme.primary),
                        "Палитра команд"
                    )
                )
            )
        ),
        Pair(
            "Настройки",
            arrayOf(
                Pair(
                    "imu_calibration",
                    Pair(
                        Pair(R.drawable.tune, MaterialTheme.colorScheme.primary),
                        "Калибровка гироскопа и акселерометра"
                    )
                ),
                Pair(
                    "motor_test",
                    Pair(
                        Pair(R.drawable.helicopter_icon, MaterialTheme.colorScheme.primary),
                        "Тестирование двигателей"
                    )
                ),
                Pair(
                    "pid_settings",
                    Pair(
                        Pair(R.drawable.controller_gen, MaterialTheme.colorScheme.primary),
                        "Настройки ПИД регуляторов"
                    )
                ),
                Pair(
                    "reports",
                    Pair(
                        Pair(R.drawable.construction, MaterialTheme.colorScheme.primary),
                        "Отчеты о полётах"
                    )
                )
            )
        )
    )
}