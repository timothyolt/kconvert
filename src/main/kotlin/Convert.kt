import com.google.common.base.CaseFormat

val builder = StringBuilder()
var ignoreVar = false
var jsonName: String = ""

fun main(args: Array<String>) {
    line@ do {
        val line = readLine() ?: break
        when {
            line.contains("class ") -> processClass(line)
            line.contains("JsonIgnore") -> ignoreVar = true
            line.contains("JsonProperty") -> jsonName = line.substring(line.indexOf('"') + 1, line.lastIndexOf('"'))
            line.contains("public ") -> processProperty(line)
        }
    } while (!line.contains(":end"))

    builder.append(')')

    println(builder.toString())
}

private fun processClass(line: String) {
    val start = line.indexOf("class ") + "class ".length
    val end = line.indexOf(' ', start)
    val name = line.substring(start, if (end > 0) end else line.length)
    builder.append("data class $name (\n")
    if (line.contains("BaseTable"))
        builder.append("\tvar id: String? = null,\n")
}

private fun processProperty(line: String) {
    when {
        ignoreVar -> {
            ignoreVar = false
            return
        }
        line.contains('(') -> return
    }
    val typeStart = line.indexOf("public ") + "public ".length
    val typeEnd = line.indexOf(' ', typeStart)
    val nameStart = typeEnd + 1
    val nameEnd = line.indexOf(' ', nameStart)
    val type = typeMap(line.substring(typeStart, if (typeEnd > 0) typeEnd else line.length))
    val name =
            if (jsonName.isNotEmpty()) jsonName
            else CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL,
                    line.substring(nameStart, if (nameEnd > 0) nameEnd else line.length))
    jsonName = ""
    if (type === "Calendar")
        builder.append("\t@JsonAdapter(CalendarAdapter::class)\n")
    builder.append("\tvar $name: $type? = null,\n")
}

private fun typeMap(type: String): String {
    return when (type) {
        "string" -> "String"
        "bool" -> "Boolean"
        "int" -> "Int"
        "long" -> "Long"
        "decimal" -> "Number"
        in arrayOf("DateTime", "DateTimeOffset") -> "Calendar"
        else -> type
    }
}