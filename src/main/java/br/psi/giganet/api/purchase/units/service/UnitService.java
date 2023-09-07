package br.psi.giganet.api.purchase.units.service;

import br.psi.giganet.api.purchase.common.webhooks.services.WebhooksHandlerService;
import br.psi.giganet.api.purchase.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.api.purchase.units.model.Unit;
import br.psi.giganet.api.purchase.units.model.UnitConversion;
import br.psi.giganet.api.purchase.units.repository.UnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UnitService {

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private WebhooksHandlerService webhooksHandlerService;

    public List<Unit> findAll() {
        return unitRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Optional<Unit> findById(Long id) {
        return unitRepository.findById(id);
    }

    public Optional<Unit> insert(Unit unit) {
        final Unit saved;
        if (unit.getConversions() != null) {
            unit.getConversions().forEach(conversion -> {
                conversion.setTo(unitRepository.findById(conversion.getTo().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada")));

                if (conversion.getTo().getConversions() == null) {
                    conversion.getTo().setConversions(new ArrayList<>());
                }
                UnitConversion inverseConversion = new UnitConversion();
                inverseConversion.setFrom(conversion.getTo());
                inverseConversion.setTo(unit);
                inverseConversion.setConversion(1 / conversion.getConversion());
                conversion.getTo().getConversions().add(inverseConversion);
            });
            saved = unitRepository.save(unit);
            unitRepository.saveAll(saved.getConversions().stream()
                    .map(UnitConversion::getTo)
                    .collect(Collectors.toList()));
        } else {
            saved = unitRepository.save(unit);
        }

        webhooksHandlerService.onSaveUnit(saved);
        return Optional.of(saved);
    }

    @Transactional
    public Optional<Unit> update(Long id, Unit unit) {
        return unitRepository.findById(id)
                .map(saved -> {
                    saved.setName(unit.getName());
                    saved.setDescription(unit.getDescription());
                    saved.setAbbreviation(unit.getAbbreviation());

                    if (unit.getConversions() != null) {

                        if (saved.getConversions() != null) { // remove conversions and inverse conversions
                            final List<UnitConversion> markToRemovalOnDestiny = new ArrayList<>();
                            final List<UnitConversion> markToRemovalOnOrigin = new ArrayList<>();
                            saved.getConversions().stream()
                                    .filter(conversion -> !unit.getConversions().contains(conversion))
                                    .forEach(conversion -> {
                                        markToRemovalOnOrigin.add(conversion);

                                        if (conversion.getTo().getConversions() != null &&
                                                conversion.getTo().getConversions().stream()
                                                        .anyMatch(c -> c.getTo().equals(conversion.getFrom()))) {

                                            markToRemovalOnDestiny.add(conversion.getTo().getConversions().stream()
                                                    .filter(c -> c.getTo().equals(conversion.getFrom()))
                                                    .findFirst().get());
                                        }
                                    });
                            markToRemovalOnDestiny.forEach(c ->
                                    c.getFrom().getConversions().removeIf(inverseConversion -> inverseConversion.equals(c)));
                            saved.getConversions().removeAll(markToRemovalOnOrigin);

                            unit.getConversions() // update existing conversions
                                    .stream()
                                    .filter(conversion -> saved.getConversions().contains(conversion))
                                    .collect(Collectors.toList())
                                    .forEach(conversion -> {
                                        int index = saved.getConversions().indexOf(conversion);
                                        UnitConversion savedConversion = saved.getConversions().get(index);
                                        savedConversion.setConversion(conversion.getConversion());

                                        UnitConversion inverseConversion;
                                        if (savedConversion.getTo().getConversions() == null) {
                                            savedConversion.getTo().setConversions(new ArrayList<>());
                                            inverseConversion = new UnitConversion();
                                            inverseConversion.setFrom(savedConversion.getTo());
                                            inverseConversion.setTo(saved);
                                            inverseConversion.setConversion(1 / savedConversion.getConversion());
                                            savedConversion.getTo().getConversions().add(inverseConversion);

                                        } else if (savedConversion.getTo().getConversions().stream()
                                                .anyMatch(c -> c.getTo().equals(savedConversion.getFrom()))) {
                                            inverseConversion = savedConversion.getTo().getConversions().stream()
                                                    .filter(c -> c.getTo().equals(savedConversion.getFrom()))
                                                    .findFirst().get();
                                            inverseConversion.setConversion(1 / savedConversion.getConversion());

                                        } else {
                                            inverseConversion = new UnitConversion();
                                            inverseConversion.setFrom(conversion.getTo());
                                            inverseConversion.setTo(saved);
                                            inverseConversion.setConversion(1 / savedConversion.getConversion());
                                            savedConversion.getTo().getConversions().add(inverseConversion);
                                        }
                                    });
                        } else {
                            saved.setConversions(new ArrayList<>());
                        }

                        unit.getConversions() // add the new conversions
                                .stream()
                                .filter(conversion -> !saved.getConversions().contains(conversion))
                                .peek(conversion -> {
                                    conversion.setFrom(saved);
                                    conversion.setTo(unitRepository
                                            .findById(conversion.getTo().getId())
                                            .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada")));

                                    if (conversion.getTo().getConversions() == null) {
                                        conversion.getTo().setConversions(new ArrayList<>());
                                    }
                                    UnitConversion inverseConversion = new UnitConversion();
                                    inverseConversion.setFrom(conversion.getTo());
                                    inverseConversion.setTo(saved);
                                    inverseConversion.setConversion(1 / conversion.getConversion());
                                    conversion.getTo().getConversions().add(inverseConversion);
                                })
                                .collect(Collectors.toList())
                                .forEach(conversion -> saved.getConversions().add(conversion));

                    } else {
                        saved.setConversions(new ArrayList<>());
                    }

                    final Unit updated = unitRepository.saveAndFlush(saved);
                    unitRepository.saveAll(updated.getConversions()
                            .stream()
                            .map(UnitConversion::getTo)
                            .collect(Collectors.toList()));

                    webhooksHandlerService.onSaveUnit(updated);

                    return updated;
                });
    }

    public Optional<Unit> deleteById(Long id) {
        return unitRepository.findById(id)
                .map(unit -> {
                    unitRepository.delete(unit);
                    return unit;
                });
    }

}
