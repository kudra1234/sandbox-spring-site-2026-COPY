<#ftl encoding="UTF-8">
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Email Confirmation</title>
</head>
<body style="margin: 0; padding: 0; background-color: #ffffff; font-family: 'Segoe UI', Arial, sans-serif;">
<table width="100%" cellpadding="0" cellspacing="0" border="0" style="background-color: #ffffff;">
    <tr>
        <td align="center" style="padding: 50px 20px;">
            <table width="100%" cellpadding="0" cellspacing="0" border="0" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 24px; max-width: 550px;">
                <tr>
                    <td style="padding: 45px 35px;">
                        <!-- Заголовок -->
                        <h1 style="text-align: center; margin: 0 0 15px 0; color: #ffffff; font-size: 28px;">
                            Reset Your Password
                        </h1>

                        <!-- Приветствие -->
                        <p style="font-size: 18px; line-height: 1.6; margin: 0 0 20px 0; color: #ffffff;">
                            Hello, ${name}!
                        </p>

                        <!-- Текст о подтверждении -->
                        <p style="font-size: 16px; line-height: 1.6; margin: 0 0 25px 0; color: #ffffff;">
                            We received a request to reset your password. Use the confirmation code below to proceed:
                        </p>

                        <!-- Код подтверждения -->
                        <table width="100%" cellpadding="0" cellspacing="0" border="0" style="margin: 30px 0;">
                            <tr>
                                <td align="center" style="background-color: rgba(255, 255, 255, 0.2); border-radius: 16px; padding: 20px;">
                                        <span style="font-size: 42px; font-weight: bold; letter-spacing: 8px; color: #ffffff;">
                                            ${password}
                                        </span>
                                </td>
                            </tr>
                        </table>

                        <!-- Инструкция -->
                        <p style="font-size: 14px; line-height: 1.6; margin: 0 0 15px 0; color: #ffffff;">
                            Use this password to log into your account. We recommend changing it after login.
                        </p>

                        <!-- Кнопка перехода на сайт -->
                        <table width="100%" cellpadding="0" cellspacing="0" border="0" style="margin: 25px 0;">
                            <tr>
                                <td align="center">
                                    <table cellpadding="0" cellspacing="0" border="0" style="margin: 0 auto;">
                                        <tr>
                                            <td align="center" style="background-color: #ffffff; border-radius: 50px; padding: 12px 35px;">
                                                <a href="https://frontendd.cloudpub.ru/reset-password.html?reset=${uuid}" style="display: inline-block; color: #667eea; text-decoration: none; font-size: 16px; font-weight: bold;">
                                                    Reset In Now
                                                </a>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>

                        <p style="font-size: 12px; line-height: 1.5; margin: 20px 0 0 0; color: rgba(255, 255, 255, 0.7);">
                            If you didn't request a password reset, please contact support immediately.
                        </p>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>
</body>
</html>