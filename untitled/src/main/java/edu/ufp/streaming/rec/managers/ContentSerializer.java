package edu.ufp.streaming.rec.managers;

import edu.ufp.streaming.rec.models.Content;
import edu.ufp.streaming.rec.models.Genre;

import java.io.*;
import java.util.List;

/**
 * Classe responsável pela serialização e deserialização binária de objetos Java.
 * Permite exportar e importar os gestores de conteúdos e géneros recorrendo à API
 * nativa do Java (Interface Serializable).
 *
 * @author Pedro
 */
public class ContentSerializer {

    // -------------------------------------------------------------------------
    // Conteúdos
    // -------------------------------------------------------------------------

    /**
     * Exporta o ContentManager para um ficheiro binário.
     */
    public static void exportContents(ContentManager cm, String filePath) {
        // try-with-resources garante que as streams são fechadas automaticamente no fim,
        // evitando Memory Leaks e ficheiros trancados pelo sistema operativo.
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {

            // Serialização Nativa do Java: transforma a estrutura inteira em bytes
            oos.writeObject(cm.listAll());
            System.out.println("[ContentSerializer] Conteúdos serializados para " + filePath);

        } catch (IOException e) {
            System.err.println("[ContentSerializer] Erro ao serializar conteúdos: " + e.getMessage());
        }
    }

    /**
     * Importa conteúdos de um ficheiro binário para o ContentManager.
     */
    @SuppressWarnings("unchecked")
    public static void importContents(ContentManager cm, String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {

            // O cast é necessário porque o Java lê um "Object" genérico.
            // @SuppressWarnings omite o aviso, pois garantimos o tipo em tempo de execução.
            List<Content> list = (List<Content>) ois.readObject();
            for (Content c : list) {
                cm.insert(c);
            }
            System.out.println("[ContentSerializer] Conteúdos deserializados de " + filePath);

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[ContentSerializer] Erro ao deserializar conteúdos: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Géneros
    // -------------------------------------------------------------------------

    /**
     * Exporta o GenreManager para um ficheiro binário.
     */
    public static void exportGenres(GenreManager gm, String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {

            oos.writeObject(gm.listAll());
            System.out.println("[ContentSerializer] Géneros serializados para " + filePath);

        } catch (IOException e) {
            System.err.println("[ContentSerializer] Erro ao serializar géneros: " + e.getMessage());
        }
    }

    /**
     * Importa géneros de um ficheiro binário para o GenreManager.
     */
    @SuppressWarnings("unchecked")
    public static void importGenres(GenreManager gm, String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {

            List<Genre> list = (List<Genre>) ois.readObject();
            for (Genre g : list) {
                gm.insert(g);
            }
            System.out.println("[ContentSerializer] Géneros deserializados de " + filePath);

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[ContentSerializer] Erro ao deserializar géneros: " + e.getMessage());
        }
    }
}