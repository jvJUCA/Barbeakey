package com.example.barbeakey;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class agendaActivity extends AppCompatActivity {

    DatabaseReference dbRef;
    String diaSelecionado = "Seg";

    String[] horarios = {
            "09:00", "09:30", "10:00", "10:30",
            "11:00", "13:00", "13:30", "14:00",
            "14:30", "15:00", "15:30", "16:00",
            "16:30", "17:00", "17:30", "18:00",
            "18:30", "19:00"
    };

    HashMap<String, Button> botoesHorario = new HashMap<>();
    Button[] botoesDias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("MeuApp", "onCreate foi chamado");
        setContentView(R.layout.activity_agenda);

        dbRef = FirebaseDatabase.getInstance().getReference("reservas");

        botoesDias = new Button[] {
                findViewById(R.id.btnSeg),
                findViewById(R.id.btnTer),
                findViewById(R.id.btnQua),
                findViewById(R.id.btnQui),
                findViewById(R.id.btnSex),
                findViewById(R.id.btnSab),
                findViewById(R.id.btnDom),
        };

        String[] nomesDias = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sab", "Dom"};

        for (int i = 0; i < botoesDias.length; i++) {
            final int index = i;
            botoesDias[i].setOnClickListener(v -> {
                diaSelecionado = nomesDias[index];
                atualizarCorDosDias(index);
                carregarHorarios();
            });
        }

        // Inicializa botões de horário
        for (String horario : horarios) {
            int resID = getResources().getIdentifier("b" + horario.replace(":", ""), "id", getPackageName());
            Button btn = findViewById(resID);
            botoesHorario.put(horario, btn);
        }

        carregarHorarios(); // carrega "Seg" ao abrir
    }

    void atualizarCorDosDias(int selecionado) {
        for (int i = 0; i < botoesDias.length; i++) {
            botoesDias[i].setBackgroundColor(i == selecionado ? Color.parseColor("#FFBB33") : Color.WHITE);
        }
    }

    void carregarHorarios() {
        Log.d("DEBUG_AGENDA", "carregarHorarios chamado para dia: " + diaSelecionado);
        for (String horario : horarios) {
            Button btn = botoesHorario.get(horario);

            if (btn != null) {
                final String h = horario;

                // Limpa estado visual do botão
                btn.setEnabled(true);
                btn.setAlpha(1.0f);
                btn.setText(h);

                dbRef.child(diaSelecionado).child(h).get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        // Já reservado
                        btn.setEnabled(false);
                        btn.setAlpha(0.5f);
                        btn.setText("Indisp.");
                    } else {
                        // Disponível – seta o clique aqui dentro
                        btn.setOnClickListener(v -> {
                            dbRef.child(diaSelecionado).child(h).setValue("reservado");
                            btn.setEnabled(false);
                            btn.setAlpha(0.5f);
                            btn.setText("Reservado");
                            Toast.makeText(this, "Horário reservado: " + h, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
        }
    }
}
