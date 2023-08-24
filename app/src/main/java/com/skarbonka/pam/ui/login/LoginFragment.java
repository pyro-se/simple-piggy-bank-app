package com.skarbonka.pam.ui.login;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.skarbonka.pam.databinding.FragmentLoginBinding;
import com.skarbonka.pam.UserData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class LoginFragment extends Fragment {
    private Executor executor = Executors.newSingleThreadExecutor();

    private FragmentLoginBinding binding;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private TextView stan_tst;
    private Button buttonLogin, buttonLogout;
    private Button buttonSave, buttonRead;
    private Button buttonDelete, buttonAuthors;
    private KeyStore keyStore;
    private Cipher cipher;
    private static final String KEY_ALIAS = "MyAppKey";
    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String USER_FILENAME = "user.json";
    private static final String TRANSACTIONS_FILENAME = "transactions.json";
    //double accountBalance;//= readAccountBalance();
    //JSONArray transactionsArray;//readTransactions();
    UserData userData = UserData.getInstance();
    double accountBalance = userData.getAccountBalance();
    JSONArray transactionsArray = userData.getTransactionsArray();
    boolean login = userData.getCzyZalogowany();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        editTextUsername = binding.editTextUsername;
        editTextPassword = binding.editTextPassword;
        buttonLogin = binding.buttonLogin;
        buttonLogout = binding.buttonLogout;
        buttonSave = binding.buttonSave;
        buttonRead = binding.buttonRead;
        buttonDelete = binding.buttonDelete;
        buttonAuthors = binding.buttonAuthors;
        stan_tst = binding.accBalance;

        //checkFingerprintPermission();

        try {
            keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
            keyStore.load(null);

            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);

            //createKey();
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        //login = userData.getCzyZalogowany();
        if(userData.getCzyZalogowany()){
            loginUser();
        }
        //UserData.getInstance().setCzyZalogowany(false);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();

                if (!username.isEmpty() && !password.isEmpty()) {
                    if (authenticateUser(username, password)) {
                        //startFingerprintAuthentication();
                        if (checkFingerprintPermission()) {
                            // Rozpocznij weryfikację odcisku palca
                            startFingerprintAuthentication();
                            /*if(userData.getCzyZalogowany()){
                                loginUser();
                            }*/
                        }
                        else{
                            UserData.getInstance().setCzyZalogowany(true);
                            loginUser();
                        }

                    } else {
                        Toast.makeText(getActivity(), "Nieprawidłowe dane logowania", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Proszę wypełnić wszystkie pola", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Wyloguj użytkownika
                logoutUser();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
                UserData.getInstance().setTransactionsArray(transactionsArray);
                UserData.getInstance().setAccountBalance(accountBalance);
                stan_tst.setText("Stan konta: " + accountBalance +" PLN");
            }
        });
        buttonRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readData();
                UserData.getInstance().setTransactionsArray(transactionsArray);
                UserData.getInstance().setAccountBalance(accountBalance);
                stan_tst.setText("Stan konta: " + accountBalance +" PLN");
            }
        });
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accountBalance = 0.00;
                transactionsArray = new JSONArray();
                saveData();
                stan_tst.setText("Stan konta: " + accountBalance +" PLN");
                readData();
            }
        });
        buttonAuthors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Created by\nŁukasz Rogowski", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    private boolean authenticateUser(String username, String password) {
        // Sprawdź poprawność danych logowania
        String hashedPassword = getMD5(password);
        return username.equals("android") && hashedPassword.equals("c803dccebff8d891bb16953e99d2917d");
    }
    private void loginUser(){
        buttonLogin.setVisibility(View.INVISIBLE);
        buttonLogout.setVisibility(View.VISIBLE);
        editTextPassword.setVisibility(View.INVISIBLE);
        editTextUsername.setVisibility(View.INVISIBLE);
        buttonSave.setVisibility(View.VISIBLE);
        buttonRead.setVisibility(View.VISIBLE);
        buttonDelete.setVisibility(View.VISIBLE);
        //readData();
        //UserData.getInstance().setTransactionsArray(transactionsArray);
        //UserData.getInstance().setAccountBalance(accountBalance);
        accountBalance = userData.getAccountBalance();
        transactionsArray = userData.getTransactionsArray();
        stan_tst.setText("Stan konta: " + accountBalance +" PLN");
    }
    private void logoutUser() {
        // Zresetuj pola formularza i ukryj przycisk wylogowania
        editTextUsername.setText("");
        editTextPassword.setText("");
        buttonLogout.setVisibility(View.INVISIBLE);
        buttonLogin.setVisibility(View.VISIBLE);
        buttonSave.setVisibility(View.INVISIBLE);
        buttonRead.setVisibility(View.INVISIBLE);
        buttonDelete.setVisibility(View.INVISIBLE);
        editTextPassword.setVisibility(View.VISIBLE);
        editTextUsername.setVisibility(View.VISIBLE);
        stan_tst.setText("");
        UserData.getInstance().setCzyZalogowany(false);
    }

    private void readData() {
        // Odczytaj dane użytkownika i transakcje z plików JSON
        accountBalance = readAccountBalance();
        transactionsArray = readTransactions();

        // Wyświetl dane w Toast (dla celów demonstracyjnych)
        Toast.makeText(getActivity(), "Transakcje: " + transactionsArray.toString(), Toast.LENGTH_LONG).show();
    }
    private void saveData(){//double accountBalance, JSONArray transactionsArray) {
        // Zapisywanie stanu konta do pliku user.json
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("account_balance", accountBalance);

            File file = new File(getActivity().getFilesDir(), USER_FILENAME);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(jsonObject.toString().getBytes());
            fos.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        // Zapisywanie transakcji do pliku transakcje.json
        try {
            File file = new File(getActivity().getFilesDir(), TRANSACTIONS_FILENAME);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(transactionsArray.toString().getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private double readAccountBalance() {
        File file = new File(getActivity().getFilesDir(), USER_FILENAME);
        double accountBalance = 0.00;

        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                byte[] data = new byte[(int) file.length()];
                fis.read(data);
                fis.close();
                String json = new String(data, StandardCharsets.UTF_8);

                JSONObject jsonObject = new JSONObject(json);
                accountBalance = jsonObject.optDouble("account_balance", 0.00);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        } else {
            // Tworzenie pliku user.json o domyślnych wartościach
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("account_balance", accountBalance);

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(jsonObject.toString().getBytes());
                fos.close();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

        return accountBalance;
    }

    private JSONArray readTransactions() {
        File file = new File(getActivity().getFilesDir(), TRANSACTIONS_FILENAME);
        JSONArray transactionsArray = new JSONArray();

        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                byte[] data = new byte[(int) file.length()];
                fis.read(data);
                fis.close();
                String json = new String(data, StandardCharsets.UTF_8);

                transactionsArray = new JSONArray(json);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
        return transactionsArray;
    }
/*
    private void createKey() {
        try {
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER);
                keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .setUserAuthenticationRequired(true)
                        .build());
                keyGenerator.generateKey();
            }
        } catch (NoSuchAlgorithmException | NoSuchProviderException |
                 InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }*/

    private void startFingerprintAuthentication() {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Zweryfikuj tożsamość")
                .setSubtitle("Użyj odcisku palca do uwierzytelnienia")
                .setNegativeButtonText("Anuluj")
                .build();

        //Executor executor;
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                UserData.getInstance().setCzyZalogowany(true);
                getView().post(new Runnable(){
                    @Override
                    public void run() {
                        loginUser();
                    }
                });
                // Uwierzytelnienie odciskiem palca zakończone pomyślnie
                // Tutaj umieścić odpowiednie działania po uwierzytelnieniu
                // Na przykład odczyt danych z pliku user.json i transakcje.json

            }
        });

        biometricPrompt.authenticate(promptInfo);
    }

    private boolean checkFingerprintPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.USE_FINGERPRINT}, 0);
            return false;
        }
        return true;
    }

    private String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            StringBuilder hashtext = new StringBuilder(no.toString(16));
            while (hashtext.length() < 32) {
                hashtext.insert(0, "0");
            }
            return hashtext.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
