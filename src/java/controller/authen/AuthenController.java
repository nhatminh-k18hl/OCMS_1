/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.authen;

import config.GlobalConfig;
import dal.AccountDAO;
import entity.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import utils.EmailUtils;
import utils.MD5PasswordEncoderUtils;

public class AuthenController extends HttpServlet {

    AccountDAO accountDAO = new AccountDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //get ve action
        String action = request.getParameter("action") != null
                ? request.getParameter("action")
                : "";
        //dua theo action set URL trang can chuyen den
        String url;
        switch (action) {
            case "login":
                url = "view/authen/login.jsp";
                break;
            case "log-out":
                url = logOut(request, response);
                break;
            case "sign-up":
                url = "view/authen/register.jsp";
                break;
            case "enter-email":
                url = "view/authen/enterEmailForgotPassword.jsp";
                break;
            default:
                url = "view/authen/login.jsp";
        }

        //chuyen trang
        request.getRequestDispatcher(url).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //get ve action
        String action = request.getParameter("action") != null
                ? request.getParameter("action")
                : "";
        //dựa theo action để xử lí request
        String url;
        switch (action) {
            case "login":
                url = loginDoPost(request, response);
                break;
            case "sign-up":
                url = signUp(request, response);
                break;
            case "verify-otp":
                url = verifyOTP(request, response);
                break;
            default:
                url = "home";
        }
        request.getRequestDispatcher(url).forward(request, response);

    }

    private String logOut(HttpServletRequest request, HttpServletResponse response) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private String loginDoPost(HttpServletRequest request, HttpServletResponse response) {
        String url = null;
        //get về các thong tin người dufg nhập
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        //kiểm tra thông tin có tồn tại trong DB ko
        Account account = Account.builder()
                .email(email)
                .password(MD5PasswordEncoderUtils.encodeMD5(password))
                .build();
        Account accFoundByUsernamePass = accountDAO.findByEmailAndPass(account);
        //true => trang home ( set account vao trong session ) 
        if (accFoundByUsernamePass != null) {
            request.getSession().setAttribute(GlobalConfig.SESSION_ACCOUNT,
                    accFoundByUsernamePass);
            url = "home";
            //false => quay tro lai trang login ( set them thong bao loi )
        } else {
            request.setAttribute("error", "Username or password incorrect!!");
            url = "view/authen/login.jsp";
        }
        return url;
    }

    private String signUp(HttpServletRequest request, HttpServletResponse response) {
        String url;
        // Lấy thông tin người dùng nhập
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // Kiểm tra mật khẩu và xác nhận mật khẩu có khớp không
        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Password and confirm password not matching");
            return "view/authen/register.jsp";
        }

        // Kiểm tra xem email đã tồn tại trong db chưa
        Account account = Account.builder()
                .email(email)
                .password(MD5PasswordEncoderUtils.encodeMD5(password))
                .role("Student")
                .status("Inactive") // Đặt trạng thái ban đầu là "Inactive"
                .build();
        Account accountFoundByEmail = accountDAO.findByEmail(account);

        if (accountFoundByEmail != null) {
            request.setAttribute("error", "Email already exists!");
            url = "view/authen/register.jsp";
        } else {
            // Lưu tài khoản vào database
            int accountId = accountDAO.insert(account);
            if (accountId > 0) {
                // Tạo session cho việc kích hoạt tài khoản sau này
                HttpSession session = request.getSession();
                account.setAccountID(accountId);
                session.setAttribute(GlobalConfig.SESSION_ACCOUNT, account);
                session.setAttribute("email", email);
                session.setMaxInactiveInterval(300);

                // Gửi OTP
                String otp = EmailUtils.sendOTPMail(email);
                session.setAttribute("otp", otp);
                session.setAttribute("otp_purpose", "activation");  // Thêm mục đích OTP

                url = "view/authen/verifyOTP.jsp";
            } else {
                request.setAttribute("error", "Failed to create account. Please try again.");
                url = "view/authen/register.jsp";
            }
        }
        return url;
    }

    private String verifyOTP(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        String storedOTP = (String) session.getAttribute("otp");
        String email = (String) session.getAttribute("email");
        String enteredOTP = request.getParameter("otp");
        String purpose = (String) session.getAttribute("otp_purpose");

        if (storedOTP != null && storedOTP.equals(enteredOTP)) {
            // OTP is correct
            session.removeAttribute("otp");

            if ("activation".equals(purpose)) {
                return handleAccountActivation(request, session);
            } else if ("password_reset".equals(purpose)) {
                return handlePasswordReset(request, session);
            } else {
                request.setAttribute("error", "Invalid OTP purpose.");
                return "view/authen/otp-verification.jsp";
            }
        } else {
            // Incorrect OTP
            request.setAttribute("error", "Incorrect OTP. Please try again.");
            return "view/authen/otp-verification.jsp";
        }
    }


    private String handleAccountActivation(HttpServletRequest request, HttpSession session) {
        Account account = (Account) session.getAttribute(GlobalConfig.SESSION_ACCOUNT);
        if (account != null) {
            account.setStatus("Active");
            accountDAO.activateAccount(account.getAccountID());
            request.setAttribute("message", "Your account has been successfully activated!");
            return "home";
        } else {
            request.setAttribute("error", "Session expired. Please sign up again.");
            return "view/authen/register.jsp";
        }
    }

    private String handlePasswordReset(HttpServletRequest request, HttpSession session) {
        // Redirect to password reset page
        return "view/authen/resetPassword.jsp";
    }

    private String resetPassword(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        String email = (String) session.getAttribute("email");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match.");
            return "view/authen/resetPassword.jsp";
        }

        Account account = Account.builder()
                .email(email)
                .password(MD5PasswordEncoderUtils.encodeMD5(newPassword))
                .build();

        boolean updated = accountDAO.updatePassword(account);
        if (updated) {
            request.setAttribute("message", "Your password has been successfully reset.");
            return "view/authen/login.jsp";
        } else {
            request.setAttribute("error", "Failed to reset password. Please try again.");
            return "view/authen/resetPassword.jsp";
        }
    }

}
