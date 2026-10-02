import javax.swing.*;
import java.awt.*;

/**
 * GUI-форма входа (iLO remote console). Поля: адрес host:port (по умолч. 443),
 * логин, пароль + advanced media/replay. Заменяет config.properties при запуске без аргументов.
 */
public class LoginDialog extends JDialog {
    private final JTextField serverField = new JTextField(22);
    private final JTextField userField = new JTextField("Administrator", 22);
    private final JPasswordField passField = new JPasswordField(22);
    private final JTextField mediaField = new JTextField("17988", 8);
    private final JTextField replayField = new JTextField("17990", 8);

    private boolean ok = false;
    public String server = "";
    public String user = "";
    public String password = "";
    public String mediaPort = "17988";
    public String replayPort = "17990";

    public LoginDialog() {
        setTitle("iLO Remote Console - вход");
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;

        int y = 0;
        y = addRow(form, c, y, "Адрес сервера (host:port):", serverField);
        y = addRow(form, c, y, "Логин:", userField);
        y = addRow(form, c, y, "Пароль:", passField);
        y = addRow(form, c, y, "Media port:", mediaField);
        y = addRow(form, c, y, "Console replay port:", replayField);

        JButton okBtn = new JButton("Подключиться");
        JButton cancelBtn = new JButton("Отмена");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(okBtn);
        buttons.add(cancelBtn);

        okBtn.addActionListener(e -> {
            collect();
            ok = true;
            dispose();
        });
        cancelBtn.addActionListener(e -> dispose());

        getRootPane().setDefaultButton(okBtn);
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }

    private int addRow(JPanel p, GridBagConstraints c, int y, String label, JComponent field) {
        c.gridx = 0;
        c.gridy = y;
        p.add(new JLabel(label), c);
        c.gridx = 1;
        c.gridy = y;
        p.add(field, c);
        return y + 1;
    }

    private void collect() {
        server = serverField.getText().trim();
        user = userField.getText().trim();
        password = new String(passField.getPassword());
        mediaPort = mediaField.getText().trim();
        replayPort = replayField.getText().trim();
        if (!server.isEmpty() && server.indexOf(':') < 0) {
            server = server + ":443";
        }
    }

    public boolean isOk() {
        return ok;
    }
}
