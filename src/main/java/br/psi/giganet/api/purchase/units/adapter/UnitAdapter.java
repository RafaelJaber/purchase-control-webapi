package br.psi.giganet.api.purchase.units.adapter;

import br.psi.giganet.api.purchase.units.controller.request.UnitRequest;
import br.psi.giganet.api.purchase.units.controller.response.UnitConversionResponse;
import br.psi.giganet.api.purchase.units.controller.response.UnitProjection;
import br.psi.giganet.api.purchase.units.controller.response.UnitResponse;
import br.psi.giganet.api.purchase.units.model.Unit;
import br.psi.giganet.api.purchase.units.model.UnitConversion;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.stream.Collectors;

@Component
public class UnitAdapter {

    public Unit create(Long id) {
        Unit unit = new Unit();
        unit.setId(id);

        return unit;
    }

    public Unit transform(UnitRequest request) {
        Unit unit = new Unit();
        unit.setName(request.getName());
        unit.setDescription(request.getDescription());
        unit.setAbbreviation(request.getAbbreviation());

        if (request.getConversions() != null) {
            unit.setConversions(
                    request.getConversions().stream()
                            .map(conversion -> {
                                UnitConversion unitConversion = new UnitConversion();
                                unitConversion.setFrom(unit);
                                unitConversion.setId(conversion.getId());
                                unitConversion.setTo(create(conversion.getTo()));
                                unitConversion.setConversion(conversion.getConversion());

                                return unitConversion;
                            })
                            .collect(Collectors.toList())
            );
        }

        return unit;
    }

    public UnitProjection transform(Unit unit) {
        UnitProjection projection = new UnitProjection();
        projection.setId(unit.getId());
        projection.setName(unit.getName());
        projection.setAbbreviation(unit.getAbbreviation());
        return projection;
    }

    @Transactional
    public UnitResponse transformToFullResponse(Unit unit) {
        UnitResponse response = new UnitResponse();
        response.setId(unit.getId());
        response.setName(unit.getName());
        response.setDescription(unit.getDescription());
        response.setAbbreviation(unit.getAbbreviation());

        if (unit.getConversions() != null) {
            response.setConversions(
                    unit.getConversions().stream()
                            .map(conversion -> {
                                UnitConversionResponse conversionResponse = new UnitConversionResponse();
                                conversionResponse.setTo(transform(conversion.getTo()));
                                conversionResponse.setConversion(conversion.getConversion());
                                conversionResponse.setId(conversion.getId());

                                return conversionResponse;
                            })
                            .collect(Collectors.toList())
            );
        }

        return response;
    }

}
