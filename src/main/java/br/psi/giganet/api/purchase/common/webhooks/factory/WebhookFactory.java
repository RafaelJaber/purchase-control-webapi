package br.psi.giganet.api.purchase.common.webhooks.factory;

import br.psi.giganet.api.purchase.common.webhooks.model.Webhook;
import br.psi.giganet.api.purchase.common.webhooks.model.WebhookServer;
import br.psi.giganet.api.purchase.common.webhooks.model.WebhookType;
import br.psi.giganet.api.purchase.products.adapter.ProductAdapter;
import br.psi.giganet.api.purchase.products.categories.adapter.CategoryAdapter;
import br.psi.giganet.api.purchase.products.categories.model.Category;
import br.psi.giganet.api.purchase.products.model.Product;
import br.psi.giganet.api.purchase.purchase_order.adapter.PurchaseOrderAdapter;
import br.psi.giganet.api.purchase.purchase_order.model.PurchaseOrder;
import br.psi.giganet.api.purchase.units.adapter.UnitAdapter;
import br.psi.giganet.api.purchase.units.model.Unit;
import br.psi.giganet.api.purchase.units.model.UnitConversion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class WebhookFactory {

    @Autowired
    private UnitAdapter unitAdapter;

    @Autowired
    private CategoryAdapter categoryAdapter;

    @Autowired
    private ProductAdapter productAdapter;

    @Autowired
    private PurchaseOrderAdapter purchaseOrderAdapter;

    public Webhook create(Unit unit) {
        List<Unit> associatedUnits = new ArrayList<>();

        Stack<Unit> stack = new Stack<>();
        stack.add(unit);

        while (!stack.isEmpty()) {
            Unit current = stack.peek();

            if (current.getConversions() == null || current.getConversions().isEmpty()) {
                associatedUnits.add(current);
                stack.pop();
            } else {
                List<Unit> unitsToAdd = current.getConversions().stream()
                        .map(UnitConversion::getTo)
                        .filter(u -> !stack.contains(u) && !associatedUnits.contains(u))
                        .collect(Collectors.toList());

                if (unitsToAdd.isEmpty()) {
                    associatedUnits.add(current);
                    stack.pop();
                } else {
                    stack.addAll(unitsToAdd);
                }
            }
        }


        Webhook webhook = new Webhook();
        webhook.setId(UUID.randomUUID().toString());
        webhook.setOrigin(WebhookServer.PURCHASE_API);
        webhook.setType(WebhookType.PURCHASE_API_SAVE_UNIT);
        webhook.setData(associatedUnits.stream()
                .map(u -> unitAdapter.transformToFullResponse(u))
                .collect(Collectors.toList()));

        return webhook;
    }

    public Webhook create(Category category) {
        Webhook webHook = new Webhook();
        webHook.setId(UUID.randomUUID().toString());
        webHook.setData(categoryAdapter.transform(category, CategoryAdapter.ResponseType.RESPONSE_WITH_PREFIX));
        webHook.setOrigin(WebhookServer.PURCHASE_API);
        webHook.setType(WebhookType.PURCHASE_API_SAVE_CATEGORY);

        return webHook;
    }

    public Webhook create(Product product) {
        Webhook webHook = new Webhook();
        webHook.setId(UUID.randomUUID().toString());
        webHook.setOrigin(WebhookServer.PURCHASE_API);
        webHook.setType(WebhookType.PURCHASE_API_SAVE_PRODUCT);

        Map<String, Object> data = new HashMap<>();
        data.put("product", productAdapter.transformToFullResponse(product));
        data.put("category", create(product.getCategory()));
        data.put("units", create(product.getUnit()));

        webHook.setData(data);

        return webHook;
    }

    public Webhook create(PurchaseOrder purchaseOrder) {
        Webhook webHook = new Webhook();
        webHook.setId(UUID.randomUUID().toString());
        webHook.setOrigin(WebhookServer.PURCHASE_API);
        webHook.setType(WebhookType.PURCHASE_API_SAVE_PURCHASE_ORDER);

        Map<String, Object> data = new HashMap<>();
        data.put("order", purchaseOrderAdapter.transformToFullWebhookResponse(purchaseOrder));
        data.put("items", purchaseOrder.getItems().stream()
                .map(item -> {
                    Map<String, Object> itemData = new HashMap<>();
                    itemData.put("product", create(item.getProduct()));
                    itemData.put("units", create(item.getUnit()));
                    return itemData;
                })
                .collect(Collectors.toList()));
        webHook.setData(data);

        return webHook;
    }
}
