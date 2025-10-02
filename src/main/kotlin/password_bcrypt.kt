package org.example

import org.mindrot.jbcrypt.BCrypt

class password_bcrypt {
    var password_true = "";
    fun hashPassword(password: String): String {
        password_true = BCrypt.hashpw(password, BCrypt.gensalt());
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    fun isValid(password: String): Boolean {
        return BCrypt.checkpw(password, password_true)
    }

}