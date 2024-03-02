package com.example.entregadores

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.example.entregadores.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var profileImageUri: Uri? = null
    private var cnhImageUri: Uri? = null
    private val firebaseStorage = FirebaseStorage.getInstance()
    private val httpClient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.uploadProfileImageButton.setOnClickListener {
            pickImage { uri ->
                profileImageUri = uri
                binding.profileImageView.setImageURI(uri)
            }
        }

        binding.uploadCnhImageButton.setOnClickListener {
            pickImage { uri ->
                cnhImageUri = uri
                binding.cnhImageView.setImageURI(uri)
            }
        }

        binding.registerButton.setOnClickListener {
            uploadImageToFirebaseStorage("profileImages", profileImageUri) { profileImageUrl ->
                uploadImageToFirebaseStorage("cnhImages", cnhImageUri) { cnhImageUrl ->
                    registerEntregador(profileImageUrl, cnhImageUrl)
                }
            }
        }
    }

    private fun pickImage(callback: (Uri) -> Unit) {
        // Define a variável global para armazenar o callback
        imageResultLauncher.launch("image/*")
        this.imagePickedCallback = callback
    }

    // Ajuste: Defina um callback de imagem e um lançador de atividade de resultado fora de onCreate()
    private var imagePickedCallback: ((Uri) -> Unit)? = null

    private val imageResultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imagePickedCallback?.invoke(it)
        }
    }

    private fun uploadImageToFirebaseStorage(folderPath: String, imageUri: Uri?, callback: (String) -> Unit) {
        if (imageUri == null) {
            callback("")
            return
        }

        val ref = firebaseStorage.reference.child("$folderPath/${System.currentTimeMillis()}")
        ref.putFile(imageUri).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { uri ->
                callback(uri.toString())
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Erro ao fazer upload da imagem.", Toast.LENGTH_SHORT).show()
            callback("")
        }
    }

    private fun registerEntregador(profileImageUrl: String, cnhImageUrl: String) {
        val name = binding.nameEditText.text.toString()
        val lastName = binding.lastNameEditText.text.toString()
        val phone = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: ""
        val cpf = binding.cpfEditText.text.toString()
        val endereco = binding.enderecoEditText.text.toString()

        // Aqui você envia os dados para o seu servidor usando OKHttp ou Retrofit
        // Exemplo:
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("name", name)
            .addFormDataPart("lastName", lastName)
            .addFormDataPart("phone", phone)
            .addFormDataPart("cpf", cpf)
            .addFormDataPart("cadasterSituation", "pendente") // ou qualquer outro valor default/necessário
            .addFormDataPart("urlCarteiraHabilitacao", cnhImageUrl)
            .addFormDataPart("urlFotoPerfil", profileImageUrl)
            .addFormDataPart("endereco", endereco)
            .build()

        val request = Request.Builder()
            .url("http://localhost:3000/entregador/register") // Use o IP real da sua máquina ou endereço do servidor
            .post(requestBody)
            .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Erro ao registrar entregador.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(applicationContext, "Entregador registrado com sucesso.", Toast.LENGTH_SHORT).show()
                        // Talvez redirecionar para outra Activity aqui
                    } else {
                        Toast.makeText(applicationContext, "Erro ao registrar entregador.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
