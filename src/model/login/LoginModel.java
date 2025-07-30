package model.login;

public class LoginModel {

    // Giả lập dữ liệu tài khoản người dùng
    public boolean authenticate(String username, String password) {
        return "admin".equals(username) && "1234".equals(password);
    }
}
