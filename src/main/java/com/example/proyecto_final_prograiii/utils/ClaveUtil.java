package com.example.proyecto_final_prograiii.utils;

import org.mindrot.jbcrypt.BCrypt;

public class ClaveUtil {
    //se manda la clave y se le hace el hash
    public static String hashClave(String clave){
        return BCrypt.hashpw(clave, BCrypt.gensalt());
    }

    //verificar una clave vs hash almacenado en la base de datos
    public static boolean verificarClave(String clave, String claveHashed){
        return BCrypt.checkpw(clave, claveHashed);
    }
}
