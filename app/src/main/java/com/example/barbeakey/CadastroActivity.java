package com.example.barbeakey;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class CadastroActivity extends AppCompatActivity {

    private EditText editTexto, editAno, editEmail, editSenha;
    private Button btnSalvar;
    private StringBuilder textoDigitado = new StringBuilder();
    private List<String> letrasEmailDisponiveis;
    private int posEmail = 0;

    private DatabaseReference databaseRef;

    private String[] todasAsLetras = {
            "A", "Á", "Â", "Ã", "B", "C", "D", "E", "É", "Ê", "F", "G",
            "H", "I", "Í", "Î", "J", "K", "L", "M", "N", "O", "Ó", "Ô", "Õ",
            "P", "Q", "R", "S", "T", "U", "Ú", "Û", "V", "W", "X", "Y", "Z"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTexto = findViewById(R.id.editTexto);
        editAno = findViewById(R.id.editAno);
        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
        btnSalvar = findViewById(R.id.btnSalvar);

        databaseRef = FirebaseDatabase.getInstance().getReference("usuarios");

        editTexto.setOnClickListener(v -> mostrarDialogoDeLetra());

        editAno.setOnClickListener(v -> {
            Calendar calendario = Calendar.getInstance();
            int ano = calendario.get(Calendar.YEAR);
            int mes = calendario.get(Calendar.MONTH);
            int dia = calendario.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (DatePicker view, int year, int month, int dayOfMonth) -> {
                        String data = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                        editAno.setText(data);
                    },
                    ano, mes, dia
            );
            dialog.show();
        });

        letrasEmailDisponiveis = new ArrayList<>(Arrays.asList(
                "a","b","c","d","e","f","g","h","i","j","k","l","m",
                "n","o","p","q","r","s","t","u","v","w","x","y","z",
                "0","1","2","3","4","5","6","7","8","9","@",".","_","-"
        ));
        Collections.shuffle(letrasEmailDisponiveis);
        editEmail.setOnClickListener(v -> mostrarLetraEmail());

        editSenha.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                String senha = s.toString();
                if (!senha.contains("~")) {
                    editSenha.setError("A senha deve conter o caractere '~'");
                    return;
                }
                int tamanho = senha.length();
                if (!isPrimo(tamanho)) {
                    editSenha.setError("O tamanho da senha deve ser um número primo");
                    return;
                }
                editSenha.setError(null);
            }
        });

        btnSalvar.setOnClickListener(v -> salvarCadastroNoFirebase());
    }

    private void mostrarDialogoDeLetra() {
        List<String> letras = Arrays.asList(todasAsLetras);
        Collections.shuffle(letras);
        CharSequence[] opcoes = letras.toArray(new CharSequence[0]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Escolha uma letra");
        builder.setItems(opcoes, (dialog, which) -> {
            String letraEscolhida = opcoes[which].toString();
            textoDigitado.append(letraEscolhida);
            editTexto.setText(textoDigitado.toString());
        });
        builder.show();
    }

    private void mostrarLetraEmail() {
        if (letrasEmailDisponiveis.isEmpty()) {
            editEmail.setError("Todas as letras foram usadas");
            return;
        }

        String letraAtual = letrasEmailDisponiveis.get(posEmail);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirme a letra do seu email");
        builder.setMessage("A letra é: " + letraAtual);
        builder.setPositiveButton("Sim", (dialog, which) -> {
            String atual = editEmail.getText().toString();
            editEmail.setText(atual + letraAtual);
            letrasEmailDisponiveis.remove(posEmail);
            if (posEmail >= letrasEmailDisponiveis.size()) posEmail = 0;
        });
        builder.setNegativeButton("Passar", (dialog, which) -> {
            posEmail++;
            if (posEmail >= letrasEmailDisponiveis.size()) posEmail = 0;
            mostrarLetraEmail();
        });
        builder.show();
    }

    private boolean isPrimo(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
        }
        return true;
    }

    private void salvarCadastroNoFirebase() {
        String nome = editTexto.getText().toString();
        String dataNascimento = editAno.getText().toString();
        String email = editEmail.getText().toString();
        String senha = editSenha.getText().toString();

        if (nome.isEmpty() || dataNascimento.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Log.e("SalvarCadastro", "Algum campo está vazio!");
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("SalvarCadastro", "Campos preenchidos com sucesso!");
        Log.d("SalvarCadastro", "Nome: " + nome);
        Log.d("SalvarCadastro", "Data de Nascimento: " + dataNascimento);
        Log.d("SalvarCadastro", "Email: " + email);
        Log.d("SalvarCadastro", "Senha: " + senha);

        String id = databaseRef.push().getKey();
        if (id == null) {
            Log.e("SalvarCadastro", "Falha ao gerar ID para o usuário!");
            return;
        }

        Log.d("SalvarCadastro", "ID gerado para o usuário: " + id);

        Usuario usuario = new Usuario(id, nome, dataNascimento, email, senha);

        Log.d("SalvarCadastro", "Usuário para salvar: " + usuario.toString());

        databaseRef.child(id).setValue(usuario)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cadastro salvo!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(CadastroActivity.this, agendaActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("SalvarCadastro", "Erro ao salvar cadastro: " + e.getMessage());
                    Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
