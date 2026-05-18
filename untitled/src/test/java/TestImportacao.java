import edu.ufp.streaming.rec.managers.ContentFileManager;
import edu.ufp.streaming.rec.managers.StreamingDatabase;
import edu.ufp.streaming.rec.managers.SeedData;

public class TestImportacao {
    public static void main(String[] args) {
        // 1. Criar a base de dados do teu projeto
        StreamingDatabase db = new StreamingDatabase();

        // 2. Carregar os géneros base (g1, g2, g5, etc.) para que a importação funcione
        SeedData.populate(db);

        // 3. Importar o ficheiro que acabaste de descarregar
        String caminhoTXT = "C:\\Users\\pedro\\IdeaProjects\\LP2_PROJECT\\untitled\\conteudos_streaming.txt";
        ContentFileManager.importContents(db.getContentManager(), db.getGenreManager(), caminhoTXT);

        // 4. Imprimir para verificar se funcionou!
        System.out.println("Foram carregados " + db.getContentManager().listAll().size() + " conteúdos no total.");

        // Podemos até imprimir o título de cada um:
        db.getContentManager().listAll().forEach(c -> System.out.println(" - " + c.getTitle()));
    }
}