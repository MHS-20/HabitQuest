package habitquest.guild.application;

public class GuildNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public GuildNotFoundException(String message) {
        super(message);
    }
}
