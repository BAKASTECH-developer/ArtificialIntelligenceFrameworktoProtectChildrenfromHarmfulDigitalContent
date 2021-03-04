package bakas.it.artificialintelligenceframeworktoprotectchildrenfromharmfuldigitalcontent;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import android.os.AsyncTask;
import android.os.Environment;

import android.widget.Toast;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Mail extends AsyncTask<Void,Void,Void> {

    private Context context;//İçerik tanımı
    private Session session;
    public String email="girayserter1@gmail.com";
    public String subject="Araç Gaz Ölçüm Verileri";
    public String message="Veriler";//Gönderilecek email tanımı
    private ProgressDialog Dialog;//Uyarı ekranı tanımı

    public Mail(Context context){
        this.context=context;

    }

    public Mail(Context context,ProgressDialog dialog){
        this.context=context;
        this.Dialog=dialog;
    }

    //Mail gönderilirken arkaplanda çalışacak işlemler
    @Override
    protected Void doInBackground(Void... params) {
        Properties props=new Properties();

        //Smtp sunucu bilgileri
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", "in-v3.mailjet.com");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        //Smtp sunucu kullanıcı şifre bilgisi
        session=Session.getDefaultInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("89fb05c328418821f8c261b13b4de697","d22e7a81f5eb48947710bce04e426507");
            }
        });

        //Mail gönderme komutları
        try{
            MimeMessage mimeMessage=new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress("info@compositeware.com"));
            mimeMessage.addRecipient(Message.RecipientType.TO,new InternetAddress(email));
            mimeMessage.setSubject(subject);
            mimeMessage.setText(message);


            Multipart multipart = new MimeMultipart();


            MimeBodyPart imgPart1 = new MimeBodyPart();
            String file = Environment.getExternalStorageDirectory() + "/Cariqveri/CO2.png";
            String fileName = "CO2.png";
            DataSource source = new FileDataSource(file);
            imgPart1.setDataHandler(new DataHandler(source));
            imgPart1.setFileName(fileName);
            multipart.addBodyPart(imgPart1);

            MimeBodyPart imgPart2 = new MimeBodyPart();
            String file2 = Environment.getExternalStorageDirectory() + "/Cariqveri/O2.png";
            String fileName2 = "O2.png";
            DataSource source2 = new FileDataSource(file2);
            imgPart2.setDataHandler(new DataHandler(source2));
            imgPart2.setFileName(fileName2);
            multipart.addBodyPart(imgPart2);

            MimeBodyPart imgPart3 = new MimeBodyPart();
            String file3 = Environment.getExternalStorageDirectory() + "/Cariqveri/CO.png";
            String fileName3 = "CO.png";
            DataSource source3 = new FileDataSource(file3);
            imgPart3.setDataHandler(new DataHandler(source3));
            imgPart3.setFileName(fileName3);
            multipart.addBodyPart(imgPart3);

            MimeBodyPart imgPart4 = new MimeBodyPart();
            String file4 = Environment.getExternalStorageDirectory() + "/Cariqveri/NOx.png";
            String fileName4 = "NOx.png";
            DataSource source4 = new FileDataSource(file4);
            imgPart4.setDataHandler(new DataHandler(source4));
            imgPart4.setFileName(fileName4);
            multipart.addBodyPart(imgPart4);

            MimeBodyPart csvPart = new MimeBodyPart();
            String file5 = "/data/data/com.car.iq/files/Veriler.csv";
            String fileName5 = "Veriler.csv";
            DataSource source5 = new FileDataSource(file5);
            csvPart.setDataHandler(new DataHandler(source5));
            csvPart.setFileName(fileName5);
            multipart.addBodyPart(csvPart);

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(message);
            multipart.addBodyPart(textPart);

            mimeMessage.setContent(multipart);
            Transport.send(mimeMessage);

        }

        //Exception yakalama
        catch (MessagingException e){
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    //İşlem tamamlandığındaki uyarı ekranı
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        broadcastUpdate("Mail_Gonderildi");
        Toast.makeText(context,"Mail Gönderildi",Toast.LENGTH_LONG).show();
        //Dialog.dismiss();
    }

    //İşlem başlangıcındaki uyarı ekranı
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        /*Dialog.setMessage("Mail gönderiliyor...");
        Dialog.show();*/
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        context.sendBroadcast(intent);
    }




}
