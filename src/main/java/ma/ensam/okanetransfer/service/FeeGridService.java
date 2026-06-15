package ma.ensam.okanetransfer.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ma.ensam.okanetransfer.domain.agency.Corridor;
import ma.ensam.okanetransfer.domain.agency.FeeGrid;
import ma.ensam.okanetransfer.dto.agency.FeeGridRequest;
import ma.ensam.okanetransfer.dto.agency.FeeGridResponse;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.repository.CorridorRepository;
import ma.ensam.okanetransfer.repository.FeeGridRepository;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FeeGridService {

    private final FeeGridRepository feeGridRepository;
    private final CorridorRepository corridorRepository;

    public FeeGridService(FeeGridRepository feeGridRepository, CorridorRepository corridorRepository) {
        this.feeGridRepository = feeGridRepository;
        this.corridorRepository = corridorRepository;
    }

    @Transactional(readOnly = true)
    public Page<FeeGridResponse> getFeeGrids(Long corridorId, Pageable pageable) {
        if (corridorId != null) {
            return feeGridRepository.findByCorridorId(corridorId, pageable).map(this::mapToResponse);
        }
        return feeGridRepository.findAll(pageable).map(this::mapToResponse);
    }

    public FeeGridResponse createFeeGrid(FeeGridRequest request) {
        validateBusinessRules(request, -1L); // -1 car c'est une nouvelle création (pas d'ID à exclure)

        Corridor corridor = corridorRepository.findById(request.getCorridorId())
                .orElseThrow(() -> new BusinessException("Corridor introuvable"));

        FeeGrid feeGrid = new FeeGrid();
        feeGrid.setCorridor(corridor);
        if (request.getValidFrom() == null) {
            request.setValidFrom(java.time.LocalDate.now());
        }
        updateEntityFromRequest(feeGrid, request);

        return mapToResponse(feeGridRepository.save(feeGrid));
    }

    public FeeGridResponse updateFeeGrid(Long id, FeeGridRequest request) {
        FeeGrid feeGrid = feeGridRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Grille de frais introuvable"));

        validateBusinessRules(request, id);

        updateEntityFromRequest(feeGrid, request);
        return mapToResponse(feeGridRepository.save(feeGrid));
    }

    public FeeGridResponse toggleActivation(Long id, boolean active) {
        FeeGrid feeGrid = feeGridRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Grille de frais introuvable"));

        if (active && feeGridRepository.hasOverlappingActiveGrids(
                feeGrid.getCorridor().getId(), feeGrid.getMinAmount(), feeGrid.getMaxAmount(), id)) {
            throw new BusinessException("Activation impossible : cette tranche chevauche une grille déjà active pour ce corridor.");
        }

        feeGrid.setActive(active);
        return mapToResponse(feeGridRepository.save(feeGrid));
    }

    @Transactional(readOnly = true)
    public byte[] exportCsv(Long corridorId) {
        List<FeeGrid> grids = corridorId != null
                ? feeGridRepository.findByCorridorId(corridorId, Pageable.unpaged()).getContent()
                : feeGridRepository.findAll();

        String header = "id,corridorId,minAmount,maxAmount,fixedFee,percentageFee,agencyCommissionRate,centralCommissionRate,validFrom,validTo,active";
        String rows = grids.stream()
                .map(grid -> String.join(",",
                        String.valueOf(grid.getId()),
                        String.valueOf(grid.getCorridor().getId()),
                        grid.getMinAmount().toPlainString(),
                        grid.getMaxAmount().toPlainString(),
                        grid.getFixedFee().toPlainString(),
                        grid.getPercentageFee().toPlainString(),
                        grid.getAgencyCommissionRate().toPlainString(),
                        grid.getCentralCommissionRate().toPlainString(),
                        grid.getValidFrom() != null ? grid.getValidFrom().toString() : "",
                        grid.getValidTo() != null ? grid.getValidTo().toString() : "",
                        String.valueOf(grid.isActive())))
                .collect(Collectors.joining("\n"));

        return (header + "\n" + rows + "\n").getBytes(StandardCharsets.UTF_8);
    }

    private void validateBusinessRules(FeeGridRequest request, Long excludeId) {
        if (request.getMinAmount().compareTo(request.getMaxAmount()) >= 0) {
            throw new BusinessException("Le montant minimum doit être strictement inférieur au montant maximum.");
        }

        BigDecimal totalCommission = request.getAgencyCommissionRate().add(request.getCentralCommissionRate());
        if (totalCommission.compareTo(new BigDecimal("100.0")) != 0) {
            throw new BusinessException("La somme de la part agence et de la part centrale doit être exactement égale à 100%.");
        }

        if (request.isActive() && feeGridRepository.hasOverlappingActiveGrids(
                request.getCorridorId(), request.getMinAmount(), request.getMaxAmount(), excludeId)) {
            throw new BusinessException("Chevauchement détecté : Une grille active existe déjà pour cette tranche de montants sur ce corridor.");
        }
    }

    private void updateEntityFromRequest(FeeGrid feeGrid, FeeGridRequest request) {
        feeGrid.setMinAmount(request.getMinAmount());
        feeGrid.setMaxAmount(request.getMaxAmount());
        feeGrid.setFixedFee(request.getFixedFee());
        feeGrid.setPercentageFee(request.getPercentageFee());
        feeGrid.setAgencyCommissionRate(request.getAgencyCommissionRate());
        feeGrid.setCentralCommissionRate(request.getCentralCommissionRate());
        feeGrid.setValidFrom(request.getValidFrom());
        feeGrid.setValidTo(request.getValidTo());
        feeGrid.setActive(request.isActive());
    }

    private FeeGridResponse mapToResponse(FeeGrid feeGrid) {
        FeeGridResponse response = new FeeGridResponse();
        response.setId(feeGrid.getId());
        response.setCorridorId(feeGrid.getCorridor().getId());
        response.setMinAmount(feeGrid.getMinAmount());
        response.setMaxAmount(feeGrid.getMaxAmount());
        response.setFixedFee(feeGrid.getFixedFee());
        response.setPercentageFee(feeGrid.getPercentageFee());
        response.setAgencyCommissionRate(feeGrid.getAgencyCommissionRate());
        response.setCentralCommissionRate(feeGrid.getCentralCommissionRate());
        response.setValidFrom(feeGrid.getValidFrom());
        response.setValidTo(feeGrid.getValidTo());
        response.setActive(feeGrid.isActive());
        return response;
    }
}