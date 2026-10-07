package pt.psoft.g1.psoftg1.readermanagement.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.repositories.ForbiddenNameRepository;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;
import pt.psoft.g1.psoftg1.usermanagement.publishers.UserEventsPublisher;
import pt.psoft.g1.psoftg1.usermanagement.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class ReaderServiceImpl implements ReaderService {
    private final ReaderRepository readerRepo;
    private final UserRepository userRepo;
    private final ReaderMapper readerMapper;
    private final GenreRepository genreRepo;
    private final ForbiddenNameRepository forbiddenNameRepository;
    private final PhotoRepository photoRepository;
    private final PasswordEncoder passwordEncoder;
    private final pt.psoft.g1.psoftg1.readermanagement.publishers.ReaderEventsPublisher readerEventsPublisher;
    private final UserEventsPublisher userEventsPublisher;


    @Override
    public ReaderDetails create(CreateReaderRequest request, String photoURI) {
        if (userRepo.findByUsername(request.getUsername()).isPresent()) {
            throw new ConflictException("Username already exists!");
        }

        Iterable<String> words = List.of(request.getFullName().split("\\s+"));
        for (String word : words){
            if(!forbiddenNameRepository.findByForbiddenNameIsContained(word).isEmpty()) {
                throw new IllegalArgumentException("Name contains a forbidden word");
            }
        }

        List<String> stringInterestList = request.getInterestList();
        List<Genre> interestList = this.getGenreListFromStringList(stringInterestList);
        /*if(stringInterestList != null && !stringInterestList.isEmpty()) {
            request.setInterestList(this.getGenreListFromStringList(stringInterestList));
        }*/

        /*
         * Since photos can be null (no photo uploaded) that means the URI can be null as well.
         * To avoid the client sending false data, photoURI has to be set to any value / null
         * according to the MultipartFile photo object
         *
         * That means:
         * - photo = null && photoURI = null -> photo is removed
         * - photo = null && photoURI = validString -> ignored
         * - photo = validFile && photoURI = null -> ignored
         * - photo = validFile && photoURI = validString -> photo is set
         * */

        MultipartFile photo = request.getPhoto();
        if(photo == null && photoURI != null || photo != null && photoURI == null) {
            request.setPhoto(null);
        }

        int count = readerRepo.getCountFromCurrentYear();
        Reader reader = readerMapper.createReader(request);
        ReaderDetails rd = readerMapper.createReaderDetails(count+1, reader, request, photoURI, interestList);

        userRepo.save(reader);
        ReaderDetails savedRd = readerRepo.save(rd);
        
        if(savedRd != null) {
            readerEventsPublisher.sendReaderCreated(savedRd);
        }
        
        return savedRd;
    }

    @Override
    @Transactional
    public ReaderDetails createWithUser(CreateReaderWithUserRequest request) {
        // 1. Check if username already exists
        if (userRepo.findByUsername(request.getUsername()).isPresent()) {
            throw new ConflictException("Username already exists!");
        }

        // 2. Validate full name
        Iterable<String> words = List.of(request.getFullName().split("\\s+"));
        for (String word : words) {
            if (!forbiddenNameRepository.findByForbiddenNameIsContained(word).isEmpty()) {
                throw new IllegalArgumentException("Name contains a forbidden word");
            }
        }

        // 3. Create User (Reader type)
        Reader reader = Reader.newReader(request.getUsername(), request.getPassword(), request.getFullName());
        reader.setPassword(passwordEncoder.encode(request.getPassword()));
        Reader savedReader = userRepo.save(reader);

        // Publish User.Created event to notify lms-authnusers
        if (savedReader != null) {
            userEventsPublisher.sendUserCreated(savedReader);
        }

        // 4. Create ReaderDetails
        List<String> stringInterestList = request.getInterestList();
        List<Genre> interestList = this.getGenreListFromStringList(stringInterestList);

        int count = readerRepo.getCountFromCurrentYear();
        
        // Create a temporary CreateReaderRequest from the combined request
        CreateReaderRequest tempRequest = new CreateReaderRequest();
        tempRequest.setUsername(request.getUsername());
        tempRequest.setPassword(request.getPassword());
        tempRequest.setFullName(request.getFullName());
        tempRequest.setBirthDate(request.getBirthDate());
        tempRequest.setPhoneNumber(request.getPhoneNumber());
        tempRequest.setGdpr(request.isGdpr());
        tempRequest.setInterestList(stringInterestList);

        ReaderDetails rd = readerMapper.createReaderDetails(count + 1, savedReader, tempRequest, null, interestList);
        ReaderDetails savedRd = readerRepo.save(rd);

        if (savedRd != null) {
            readerEventsPublisher.sendReaderCreated(savedRd);
        }

        return savedRd;
    }

    // Comentado - Lending não existe neste microserviço
    // @Override
    // public List<ReaderBookCountDTO> findTopByGenre(String genre, LocalDate startDate, LocalDate endDate){
    //     if(startDate.isAfter(endDate)){
    //         throw new IllegalArgumentException("Start date cannot be after end date");
    //     }
    //     Pageable pageableRules = PageRequest.of(0,5);
    //     return this.readerRepo.findTopByGenre(pageableRules, genre, startDate, endDate).getContent();
    // }

    @Override
    @Transactional
    public ReaderDetails update(final Long id, final UpdateReaderRequest request, final long desiredVersion, String photoURI){
        final ReaderDetails readerDetails = readerRepo.findByUserId(id)
                .orElseThrow(() -> new NotFoundException("Cannot find reader"));

        List<String> stringInterestList = request.getInterestList();
        List<Genre> interestList = this.getGenreListFromStringList(stringInterestList);

         /*
         * Since photos can be null (no photo uploaded) that means the URI can be null as well.
         * To avoid the client sending false data, photoURI has to be set to any value / null
         * according to the MultipartFile photo object
         *
         * That means:
         * - photo = null && photoURI = null -> photo is removed
         * - photo = null && photoURI = validString -> ignored
         * - photo = validFile && photoURI = null -> ignored
         * - photo = validFile && photoURI = validString -> photo is set
         * */

        MultipartFile photo = request.getPhoto();
        if(photo == null && photoURI != null || photo != null && photoURI == null) {
            request.setPhoto(null);
        }

        readerDetails.applyPatch(desiredVersion, request, photoURI, interestList);

        userRepo.save(readerDetails.getReader());
        ReaderDetails updatedRd = readerRepo.save(readerDetails);
        
        if(updatedRd != null) {
            readerEventsPublisher.sendReaderUpdated(updatedRd, desiredVersion);
        }
        
        return updatedRd;
    }


    @Override
    public Optional<ReaderDetails> findByReaderNumber(String readerNumber) {
        return this.readerRepo.findByReaderNumber(readerNumber);
    }

    @Override
    public List<ReaderDetails> findByPhoneNumber(String phoneNumber) {
        return this.readerRepo.findByPhoneNumber(phoneNumber);
    }

    @Override
    public Optional<ReaderDetails> findByUsername(final String username) {
        return this.readerRepo.findByUsername(username);
    }


    @Override
    public Iterable<ReaderDetails> findAll() {
        return this.readerRepo.findAll();
    }

    // Comentado - Lending não existe neste microserviço
    // @Override
    // public List<ReaderDetails> findTopReaders(int minTop) {
    //     if(minTop < 1) {
    //         throw new IllegalArgumentException("Minimum top reader must be greater than 0");
    //     }

    //     Pageable pageableRules = PageRequest.of(0,minTop);
    //     Page<ReaderDetails> page = readerRepo.findTopReaders(pageableRules);
    //     return page.getContent();
    // }

    private List<Genre> getGenreListFromStringList(List<String> interestList) {
        if(interestList == null) {
            return null;
        }

        if(interestList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Genre> genreList = new ArrayList<>();
        for(String interest : interestList) {
            Optional<Genre> optGenre = genreRepo.findByString(interest);
            if(optGenre.isEmpty()) {
                throw new NotFoundException("Could not find genre with name " + interest);
            }

            genreList.add(optGenre.get());
        }

        return genreList;
    }

    @Override
    public Optional<ReaderDetails> removeReaderPhoto(String readerNumber, long desiredVersion) {
        ReaderDetails readerDetails = readerRepo.findByReaderNumber(readerNumber)
                .orElseThrow(() -> new NotFoundException("Cannot find reader"));

        String photoFile = readerDetails.getPhoto().getPhotoFile();
        readerDetails.removePhoto(desiredVersion);
        Optional<ReaderDetails> updatedReader = Optional.of(readerRepo.save(readerDetails));
        photoRepository.deleteByPhotoFile(photoFile);
        return updatedReader;
    }

    @Override
    public List<ReaderDetails> searchReaders(pt.psoft.g1.psoftg1.shared.services.Page page, SearchReadersQuery query) {
        if (page == null)
            page = new pt.psoft.g1.psoftg1.shared.services.Page(1, 10);

        if (query == null)
            query = new SearchReadersQuery("", "","");

        final var list = readerRepo.searchReaderDetails(page, query);

        if(list.isEmpty())
            throw new NotFoundException("No results match the search query");

        return list;
    }

    /**
     * Cria/sincroniza um Reader a partir de dados AMQP
     * Usado quando o microserviço recebe eventos de criação de readers de outros microserviços
     */
    @Override
    public ReaderDetails create(pt.psoft.g1.psoftg1.readermanagement.api.ReaderViewAMQP readerViewAMQP) {
        // Verifica se já existe
        Optional<ReaderDetails> existing = readerRepo.findByUsername(readerViewAMQP.getEmail());
        if (existing.isPresent()) {
            return existing.get();
        }

        // Cria user mínimo se não existir
        Optional<pt.psoft.g1.psoftg1.usermanagement.model.User> userOpt = 
            userRepo.findByUsername(readerViewAMQP.getEmail());
        
        if (userOpt.isEmpty()) {
            // Sincroniza user básico
            pt.psoft.g1.psoftg1.usermanagement.model.Reader newReader = 
                pt.psoft.g1.psoftg1.usermanagement.model.Reader.newReader(
                    readerViewAMQP.getEmail(), "default", readerViewAMQP.getName());
            userRepo.save(newReader);
        }

        // Retorna reader existente ou cria placeholder
        return existing.orElse(null);
    }

    /**
     * Atualiza um Reader a partir de dados AMQP
     */
    @Override
    public ReaderDetails update(pt.psoft.g1.psoftg1.readermanagement.api.ReaderViewAMQP readerViewAMQP) {
        Optional<ReaderDetails> existing = readerRepo.findByUsername(readerViewAMQP.getEmail());
        if (existing.isEmpty()) {
            // Se não existe, cria
            return create(readerViewAMQP);
        }

        // Atualiza dados básicos se necessário
        ReaderDetails readerDetails = existing.get();
        pt.psoft.g1.psoftg1.usermanagement.model.Reader reader = readerDetails.getReader();
        if (reader != null) {
            reader.setName(readerViewAMQP.getName());
            userRepo.save(reader);
        }

        return readerDetails;
    }
}
