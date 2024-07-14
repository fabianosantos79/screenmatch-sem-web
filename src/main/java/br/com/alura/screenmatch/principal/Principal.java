package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner leitura = new Scanner(System.in);
    private final String ENDERECO = "https://www.omdbapi.com/?apikey=cb2aed93&t=";
    private ConsumoApi consumoApi = new ConsumoApi();
    private ConverteDados converteDados = new ConverteDados();
    private List<DadosTemporada> temporadas = new ArrayList<>();

    public void exibeMenu(){
        System.out.println("Digite o nome da série:");
        var nomeSerie = leitura.nextLine();
        var json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+"));

        DadosSerie dadosSerie = converteDados.obterDados(json, DadosSerie.class);
        System.out.println(dadosSerie);

		for (int i = 1; i <= dadosSerie.totalTemporadas() ; i++) {
			json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i);
			DadosTemporada dadosTemporada = converteDados.obterDados(json, DadosTemporada.class);
			temporadas.add(dadosTemporada);
		}
		temporadas.forEach(System.out::println);

        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));
        temporadas.forEach(t -> System.out.println(t.numero()));

//        List<String> nomes = Arrays.asList("Fabiano", "Viviane", "Marcio", "Fernando", "Isabela", "Ricardo", "Nicole", "Expedito", "Maria", "Zezinho");
//        nomes.stream().sorted().limit(5).map(String::toUpperCase).filter(t -> t.startsWith("F")).forEach(System.out::println);

        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()).toList();

        System.out.println("\n * Todos episódios *");
        dadosEpisodios.forEach(System.out::println);

        System.out.println("\n *** Top 5 episódios ***");

        dadosEpisodios.stream()
                .filter(e -> !e.avaliacao().equals("N/A"))
                .peek(e -> System.out.println("PRIMEIRO FILTRO " + e))
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                .peek(e -> System.out.println("ORDENAÇÃO " + e))
                .limit(5)
                .peek(e -> System.out.println("LIMITE " + e))
                .forEach(System.out::println);

        System.out.println("\n === Todos episódios por temporada ===");
        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream().map(d -> new Episodio(t.numero(), d)))
                        .collect(Collectors.toList());

        episodios.forEach(System.out::println);

        System.out.println("A partir de que ano você deseja buscar oe episódios?");
        var ano = leitura.nextInt();
        leitura.nextLine();

        System.out.println(" ");
        System.out.println("^^^^^ Episódios a partir do ano " + ano + " ^^^^^");

        LocalDate dataBusca = LocalDate.of(ano, 1,1);

        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        episodios.stream().filter(e -> e.getData() != null && e.getData().isAfter(dataBusca)).forEach(e -> System.out.println(
                "Temporada: " + e.getTemporada() +
                        " Episódio: " + e.getTitulo()+
                        " Data de Lançamento: " + e.getData().format(formatador)
        ));

        System.out.println("Digite um termo para busca de um episódio");
        var trechoTitulo = leitura.nextLine();

        Optional<Episodio> episodioBuscado = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
                .findFirst();
        if(episodioBuscado.isPresent()){
            System.out.println("Episodio encontrado: " + episodioBuscado.get().getTitulo() );
            System.out.println("Temporada: " + episodioBuscado.get().getTemporada());
        }else{
            System.out.println("Episódio não encontrado");
        }

        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getAvaliacao)));

        System.out.println("Avaliação por Temporada");
        System.out.println(avaliacoesPorTemporada);

        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));

        System.out.println("Estatísticas: " + est);
        System.out.println("Contagem: " + est.getCount());
        System.out.println("Média: " + est.getAverage());
        System.out.println("Melhor episódio: " + est.getMax());
        System.out.println("Pior episódio: " + est.getMin());

    }
}
