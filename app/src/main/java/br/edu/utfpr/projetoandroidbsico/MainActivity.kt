package br.edu.utfpr.projetoandroidbsico

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Parcel
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity() : AppCompatActivity() {

    fun exibirCodigo (titulo: String?, texto: String?) {
        val mensagem: AlertDialog.Builder = AlertDialog.Builder(this)
        mensagem.setTitle(titulo)
        mensagem.setMessage(texto)
        mensagem.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        mensagem.show()
    }

    private lateinit var etCodigo : EditText
    private lateinit var etCidadeAbastecimento : EditText
    private lateinit var etQuantidadeLitros : EditText

    private lateinit var banco : SQLiteDatabase

    constructor(parcel: Parcel) : this() {

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        etCodigo = findViewById( R.id.etCodigo )
        etCidadeAbastecimento = findViewById( R.id.etCidadeAbastecimento )
        etQuantidadeLitros = findViewById( R.id.etQuantidadeLitros )

        //Local de Armazenamento do Banco
        banco = SQLiteDatabase.openOrCreateDatabase(this.getDatabasePath("dbfile.sqlite"), null)

        //Criação da tabela e seus campos
        banco.execSQL("CREATE TABLE IF NOT EXISTS abastecimentos(_id INTEGER PRIMARY KEY AUTOINCREMENT, codigo_abastecimento INTEGER, cidade TEXT, quantidade_litros REAL)")

    }

    fun btListarOnClick(view: View) {
        //chamamento da função exibir código como AlertDialog
        exibirCodigo ("Inclua o Código dos Combustíveis de acordo com a lista abaixo:",
            "1 - Gasolina\n2 - Etanol\n3 - Diesel\n4 - Gás Natural")
    }

    fun btIncluirOnClick(view: View) {
        //Validação para preenchimento do campos
        if (etCodigo.getText().toString() == "") {
            etCodigo.setError("O campo Código do combustível deve ser preenchido") // erro a ser retornado

            etCodigo.requestFocus()
            return
        }

        if (etCidadeAbastecimento.getText().toString() == "") {
            etCidadeAbastecimento.setError("O campo Cidade deve ser preenchido") // erro a ser retornado

            etCidadeAbastecimento.requestFocus()
            return
        }

        if (etQuantidadeLitros.getText().toString() == "") {
            etQuantidadeLitros.setError("O campo Quantidade deve ser preenchido") // erro a ser retornado

            etQuantidadeLitros.requestFocus()
            return
        }
        val registro = ContentValues()
        registro.put("codigo_abastecimento",etCodigo.text.toString().toInt())
        registro.put("cidade", etCidadeAbastecimento.text.toString())
        registro.put("quantidade_litros", etQuantidadeLitros.text.toString().toDouble())

        //para fazer a inclusão no banco vamos usar os dados abaixo com 3 parametros
        banco.insert("abastecimentos", null, registro)

        //apresentação da resposta ao banco via Toast
        val codigoCombustivel = etCodigo.text.toString().toInt()

        // Consulta pra contar as ocorrências do código de combustível na tabela "abastecimentos"
        val query = "SELECT COUNT(*) FROM abastecimentos WHERE codigo_abastecimento = ?"
        val parametros = arrayOf(codigoCombustivel.toString())

        // Código para executar a consulta
        val cursor = banco.rawQuery(query, parametros)

        var quantidadeRepeticoes = 0

        if (cursor != null) {
            cursor.moveToFirst()
            quantidadeRepeticoes = cursor.getInt(0)
            cursor.close()
        }

        Toast.makeText(this, "Registro de Abastecimento.\n Combustível ${codigoCombustivel}: " + quantidadeRepeticoes.toString(),
            Toast.LENGTH_SHORT).show()

    }

    @SuppressLint("Range")
    fun btDadosOnClick(view: View) {
        // Consulta pra calcular a somatória da quantidade abastecida para cada código de combustível
        val query = "SELECT codigo_abastecimento, SUM(quantidade_litros) AS total_quantidade_litros FROM abastecimentos GROUP BY codigo_abastecimento"
        val cursor = banco.rawQuery(query, null)

        // Mapa pra armazenamento dos resultados
        val mapaResultados = mutableMapOf<Int, Double>()

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val codigoCombustivel = cursor.getInt(cursor.getColumnIndex("codigo_abastecimento"))
                val totalQuantidadeLitros = cursor.getDouble(cursor.getColumnIndex("total_quantidade_litros"))
                mapaResultados[codigoCombustivel] = totalQuantidadeLitros
            }
            cursor.close()
        }
        //Mudando de código para nome dos combustíveis
        val codigoParaNome = mapOf(1 to "Gasolina", 2 to "Etanol", 3 to "Diesel", 4 to "Gás Natural")

        // Criando uma string para exibir os resultados com os nomes
        val resultString = StringBuilder()

        for ((codigoCombustivel, totalQuantidadeLitros) in mapaResultados) {
            val nomeCombustivel = codigoParaNome[codigoCombustivel]
            if (nomeCombustivel != null) {
                resultString.append("Combustível: ${nomeCombustivel}," +
                        " Quantidade Total Abastecida: ${totalQuantidadeLitros}\n")
            }
        }

        // Criando o AlertDialog
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Somatória por Código de Combustível")
        alertDialogBuilder.setMessage(resultString.toString())
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        // Exibição do AlertDialog
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()

        }

    }


