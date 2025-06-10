package com.example.barbeakey;

public class Usuario {
    public String id;
    public String nome;
    public String dataNascimento;
    public String email;
    public String senha;

    public Usuario() { }

    public Usuario(String id, String nome, String dataNascimento, String email, String senha) {
        this.id = id;
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.email = email;
        this.senha = senha;
    }
}
