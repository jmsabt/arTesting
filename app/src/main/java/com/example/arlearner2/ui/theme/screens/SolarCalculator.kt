import java.io.BufferedReader
import java.io.InputStreamReader

fun runSolarCalcPython(length: Float, width: Float, monthlyKwh: Float, areaPercent: Float): String? {
    try {
        val pythonExecutable = "/usr/bin/python3"  // Adjust path for your environment
        val scriptPath = "/path/to/solar_calc.py"  // Path to the Python script

        val processBuilder = ProcessBuilder(
            pythonExecutable,
            scriptPath,
            length.toString(),
            width.toString(),
            monthlyKwh.toString(),
            areaPercent.toString()
        )

        val process = processBuilder.start()
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val output = StringBuilder()
        var line: String? = reader.readLine()

        while (line != null) {
            output.append(line)
            line = reader.readLine()
        }

        val exitCode = process.waitFor()
        return if (exitCode == 0) output.toString() else null
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}
