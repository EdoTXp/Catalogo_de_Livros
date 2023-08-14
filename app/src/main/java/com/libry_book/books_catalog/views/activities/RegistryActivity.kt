/*
 * Copyright (c) 2023. Está classe está sendo consedida para uso pessoal
 */
package com.libry_book.books_catalog.views.activities

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.libry_book.books_catalog.R
import com.libry_book.books_catalog.gestores.GestorVibrator
import com.libry_book.books_catalog.storage.SharedPreferencesTheme
import com.libry_book.books_catalog.viewmodel.RegistryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegistryActivity : AppCompatActivity(), View.OnClickListener {
    //ATRIBUTOS
    private lateinit var edtTitleBook: EditText
    private lateinit var edtAuthorBook: EditText
    private lateinit var edtYearBook: EditText
    private lateinit var registryViewModel: RegistryViewModel
    private var clickEventIsClicked =
        false //variável utilizada para saber se já foi clicado mais de uma vez

    override fun onCreate(savedInstanceState: Bundle?) {
        //Método para setar o tema da activity ao iniciar
        val sharedPreferencesTheme = SharedPreferencesTheme(this)
        sharedPreferencesTheme.setAppTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_cadastrar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.title_activity_tela_cadastrar)

        //ATRIBUTO LOCAL
        val btnSave = findViewById<Button>(R.id.btnSalvar)
        edtTitleBook = findViewById(R.id.edtTitulo)
        edtAuthorBook = findViewById(R.id.edtAutor)
        edtYearBook = findViewById(R.id.edtAno)
        registryViewModel = ViewModelProvider(this)[RegistryViewModel::class.java]
        //EVENTOS
        /*
         * Método para fazer o cadastro usando o teclado do dispositivo
         *
         * */
        edtYearBook.setOnKeyListener { _: View?, keyCode: Int, event: KeyEvent ->
            /*
             * Ao envocar o evento, será feito uma filtragem capturando somente a ação ACTION_UP.
             * Em seguida, será capturada a tecla enter para chamar a ação de onClick*/
            if (event.action == KeyEvent.ACTION_UP) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    onClick(btnSave)
                    return@setOnKeyListener true
                }
            }
            return@setOnKeyListener false
        }
        btnSave.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        if (view.id == R.id.btnSalvar && !clickEventIsClicked) {
            //clickEventIsClicked vira true
            clickEventIsClicked = true

            /* removendo os espaços brancos ao final da string
             e caso a string tiver só espaços brancos, a verificação de string vazias
             nas instruções abaixo dara um valor false */
            edtTitleBook.setText(
                edtTitleBook.text.toString().trim { it <= ' ' })
            edtAuthorBook.setText(edtAuthorBook.text.toString().trim { it <= ' ' })

            //Valores para saber se o campo respectivo estiver vazio
            val title = checkEditText(edtTitleBook)
            val author = checkEditText(edtAuthorBook)
            val year = checkEditText(edtYearBook)

            if (title && author && year) {
                val titleBook = edtTitleBook.text.toString()
                val authorBook = edtAuthorBook.text.toString()
                val yearBook = edtYearBook.text.toString()
                if (registryViewModel.registerBooks(titleBook, authorBook, yearBook)) {
                    //imprimir a notificação
                    // printNotification()

                    // limpando os campos e passando o foco ao edtTitleBook
                    edtTitleBook.setText("")
                    edtAuthorBook.setText("")
                    edtYearBook.setText("")
                    edtTitleBook.requestFocus()
                } else {
                    val msg = getString(R.string.error_msg)
                    GestorVibrator.vibrate(100L, view.context)
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                }
            } else {
                GestorVibrator.vibrate(100L, view.context)
            }

            // handler utilizado para dar um delay ao evento de clicar para previnir multiplos cliques
            Handler(Looper.getMainLooper()).postDelayed({ clickEventIsClicked = false }, 1000)
        }
    }

    private fun checkEditText(edt: EditText): Boolean {
        /*Este método verifica se o EditText edt, passado como parametro, está vazio ou não.
         * Caso estiver vazio, o background do edt será alterado para o error_border e
         *  será mostrado uma animação e um text dizendo que o campo está vazio
         * No outro caso será resetado o edt com os valores padrão*/
        val actualPadding =
            edt.paddingTop // serve para pegar o atual padding e coloca-lo nas próximas alterações
        return if (TextUtils.isEmpty(edt.text)) {
            //criação da animação
            val errorAnimation = ObjectAnimator.ofFloat(edt, "translationX", 20f)
            errorAnimation.duration = 150 // a duração total da animação será de 150 millisegundos
            errorAnimation.repeatMode = ValueAnimator.REVERSE // volta no local padrão
            errorAnimation.repeatCount = 3 // repetir a animação 3 vezes
            edt.hint = getString(R.string.hint_error)
            edt.background = ContextCompat.getDrawable(this, R.drawable.layout_border_error)
            edt.setPadding(
                actualPadding,
                actualPadding,
                actualPadding,
                actualPadding
            ) //ajustando o padding
            errorAnimation.start()
            GestorVibrator.vibrate(200, edt.context)
            false
        } else {
            edt.background = ContextCompat.getDrawable(this, R.drawable.layout_border)
            edt.setPadding(
                actualPadding,
                actualPadding,
                actualPadding,
                actualPadding
            ) //ajustando o padding

            when (edt.id) {
                R.id.edtTitulo -> edt.hint =
                    getString(R.string.hint_titulo)

                R.id.edtAutor -> edt.hint =
                    getString(R.string.hint_autor)

                else -> edt.hint = getString(R.string.hint_ano)
            }
            true
        }
    }

    /*  private fun printNotification() {

          // TODO Correge l'errore della notifica quando usa Android 13

          val bodyTextNotification =
              getString(R.string.Notification_Text_1) + " \"" + edtTitleBook.text.toString() + "\" " + getString(
                  R.string.Notification_Text_2
              )
          val notification = GestorNotification(
              this,
              R.drawable.transparent_icon_app,
              getString(R.string.Notification_Title),
              bodyTextNotification,
              2
          )
          notification.setColor(R.color.colorPrimary)
          notification.setDurationVibrate(longArrayOf(0L, 200L, 150L, 200L))
          notification.setSound(
              Uri.parse(
                  "android.resource://"
                          + baseContext.packageName
                          + "/"
                          + R.raw.recycle
              )
          )
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
              notification.createChannelId(
                  getString(R.string.Notification_Channel),
                  getString(R.string.Notification_Description),
                  getSystemService(NotificationManager::class.java)
              )
          }
          notification.printNotification()
      }*/
}