import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLInjectionTester {
    public static void main(String[] args) {
        JFrame frame = new JFrame("SQL Injection Tester");
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        JLabel label = new JLabel("Enter URL:");
        label.setBounds(10, 10, 80, 25);
        frame.add(label);

        JTextField urlField = new JTextField();
        urlField.setBounds(100, 10, 280, 25);
        frame.add(urlField);

        JButton testButton = new JButton("Test");
        testButton.setBounds(150, 50, 100, 25);
        frame.add(testButton);

        JLabel resultLabel = new JLabel();
        resultLabel.setBounds(10, 90, 380, 25);
        frame.add(resultLabel);

        testButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String urlString = urlField.getText();
                String result = testForSQLInjection(urlString);
                resultLabel.setText(result);
            }
        });

        frame.setVisible(true);
    }

    public static String testForSQLInjection(String urlString) {
        try {
            // Connect to the website
            @SuppressWarnings("deprecation")
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Check for input fields
            String html = response.toString();
            Pattern pattern = Pattern.compile("<input[^>]*>");
            Matcher matcher = pattern.matcher(html);

            if (!matcher.find()) {
                return "No input fields found.";
            }

            // Try SQL Injection
            @SuppressWarnings("deprecation")
            URL postUrl = new URL(urlString);
            HttpURLConnection postConn = (HttpURLConnection) postUrl.openConnection();
            postConn.setRequestMethod("POST");
            postConn.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(postConn.getOutputStream());
            writer.write("test=' OR 1=1 --");
            writer.flush();
            writer.close();

            int responseCode = postConn.getResponseCode();
            if (responseCode == 200) {
                return "The website is potentially vulnerable to SQL Injection.";
            } else {
                return "The website does not seem to be vulnerable to SQL Injection.";
            }
        } catch (Exception e) {
            return "An error occurred: " + e.getMessage();
        }
    }
}
