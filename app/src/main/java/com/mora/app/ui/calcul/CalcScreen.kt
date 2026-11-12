package com.mora.app.ui.calcul

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private val fmt = DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale.FRANCE))

private fun format(v: Double): String =
    if (v.isNaN() || v.isInfinite()) "Error" else fmt.format(v)

private fun parse(s: String): Double? = s.replace(',', '.').replace(" ", "").toDoubleOrNull()

@Composable
fun CalcScreen(onBack: () -> Unit) {
    var tab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Hiverina") }
        }
        Text(
            "Calcul",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Mandeha tsy mila internet. Vola: Ariary (Ar).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Kajy", "Isan-jato", "Fihenam-bidy").forEachIndexed { i, label ->
                if (tab == i) {
                    Button(onClick = { tab = i }) { Text(label) }
                } else {
                    FilledTonalButton(onClick = { tab = i }) { Text(label) }
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        when (tab) {
            0 -> BasicCalc()
            1 -> PercentCalc()
            else -> DiscountCalc()
        }
    }
}

@Composable
private fun BasicCalc() {
    var display by remember { mutableStateOf("0") }
    var stored by remember { mutableStateOf<Double?>(null) }
    var op by remember { mutableStateOf<String?>(null) }
    var fresh by remember { mutableStateOf(true) }

    fun compute(a: Double, b: Double, o: String): Double = when (o) {
        "+" -> a + b
        "−" -> a - b
        "×" -> a * b
        "÷" -> if (b == 0.0) Double.NaN else a / b
        else -> b
    }

    fun digit(d: String) {
        display = if (fresh || display == "0") d else display + d
        fresh = false
    }

    fun dot() {
        if (fresh) { display = "0."; fresh = false }
        else if (!display.contains(".")) display += "."
    }

    fun operator(o: String) {
        val cur = display.toDoubleOrNull() ?: return
        val s = stored
        val p = op
        if (s != null && p != null && !fresh) {
            val r = compute(s, cur, p)
            stored = r
            display = if (r.isNaN()) "Error" else format(r).replace(" ", "").replace(',', '.')
        } else {
            stored = cur
        }
        op = o
        fresh = true
    }

    fun equals() {
        val cur = display.toDoubleOrNull() ?: return
        val s = stored
        val p = op
        if (s != null && p != null) {
            val r = compute(s, cur, p)
            display = if (r.isNaN()) "Error" else format(r).replace(" ", "").replace(',', '.')
        }
        stored = null
        op = null
        fresh = true
    }

    fun clear() { display = "0"; stored = null; op = null; fresh = true }

    fun backspace() {
        if (fresh) return
        display = if (display.length > 1) display.dropLast(1) else "0"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = if (op != null && stored != null) "${format(stored!!)} $op" else " ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
            Text(
                text = display,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                maxLines = 1
            )
        }
    }
    Spacer(Modifier.height(12.dp))

    val rows = listOf(
        listOf("C", "⌫", "÷", "×"),
        listOf("7", "8", "9", "−"),
        listOf("4", "5", "6", "+"),
        listOf("1", "2", "3", "="),
        listOf("0", ".", "", "")
    )
    rows.forEach { r ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            r.forEach { k ->
                if (k.isEmpty()) {
                    Spacer(Modifier.weight(1f))
                } else {
                    val isOp = k in listOf("÷", "×", "−", "+", "=")
                    Button(
                        onClick = {
                            when (k) {
                                "C" -> clear()
                                "⌫" -> backspace()
                                "." -> dot()
                                "=" -> equals()
                                "÷", "×", "−", "+" -> operator(k)
                                else -> digit(k)
                            }
                        },
                        modifier = Modifier.weight(1f).height(60.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = if (isOp) ButtonDefaults.buttonColors()
                        else ButtonDefaults.filledTonalButtonColors()
                    ) {
                        Text(k, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultCard(lines: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            lines.forEach { (label, value) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        value,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun NumField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PercentCalc() {
    var base by remember { mutableStateOf("") }
    var pct by remember { mutableStateOf("") }
    val b = parse(base)
    val p = parse(pct)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Isan-jato (%)", style = MaterialTheme.typography.titleMedium)
        NumField("Isa (Ar)", base) { base = it }
        NumField("Isan-jato (%)", pct) { pct = it }
        if (b != null && p != null) {
            val part = b * p / 100.0
            ResultCard(
                listOf(
                    "$pct% an'ny ${format(b)}" to "${format(part)} Ar",
                    "Isa + $pct%" to "${format(b + part)} Ar",
                    "Isa − $pct%" to "${format(b - part)} Ar"
                )
            )
        }
    }
}

@Composable
private fun DiscountCalc() {
    var price by remember { mutableStateOf("") }
    var disc by remember { mutableStateOf("") }
    val pr = parse(price)
    val d = parse(disc)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Fihenam-bidy sy vidiny farany", style = MaterialTheme.typography.titleMedium)
        NumField("Vidiny voalohany (Ar)", price) { price = it }
        NumField("Fihenam-bidy (%)", disc) { disc = it }
        if (pr != null && d != null) {
            val saved = pr * d / 100.0
            ResultCard(
                listOf(
                    "Vola voavonjy" to "${format(saved)} Ar",
                    "Vidiny farany" to "${format(pr - saved)} Ar"
                )
            )
        }
    }
}
