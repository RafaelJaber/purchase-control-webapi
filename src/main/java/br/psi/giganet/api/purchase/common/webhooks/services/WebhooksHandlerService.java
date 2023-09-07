package br.psi.giganet.api.purchase.common.webhooks.services;

import br.psi.giganet.api.purchase.common.webhooks.factory.WebhookFactory;
import br.psi.giganet.api.purchase.common.webhooks.model.Webhook;
import br.psi.giganet.api.purchase.common.webhooks.model.WebhookServer;
import br.psi.giganet.api.purchase.common.webhooks.model.WebhookType;
import br.psi.giganet.api.purchase.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.api.purchase.products.adapter.ProductAdapter;
import br.psi.giganet.api.purchase.products.categories.adapter.CategoryAdapter;
import br.psi.giganet.api.purchase.products.categories.model.Category;
import br.psi.giganet.api.purchase.products.categories.service.CategoryService;
import br.psi.giganet.api.purchase.products.model.Product;
import br.psi.giganet.api.purchase.products.service.ProductService;
import br.psi.giganet.api.purchase.purchase_order.adapter.PurchaseOrderAdapter;
import br.psi.giganet.api.purchase.purchase_order.model.PurchaseOrder;
import br.psi.giganet.api.purchase.purchase_order.service.PurchaseOrderService;
import br.psi.giganet.api.purchase.units.adapter.UnitAdapter;
import br.psi.giganet.api.purchase.units.model.Unit;
import br.psi.giganet.api.purchase.units.service.UnitService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class WebhooksHandlerService extends AbstractWebhookService {

    @Autowired
    private ProductAdapter productAdapter;

    @Autowired
    private UnitAdapter unitAdapter;

    @Autowired
    private CategoryAdapter categoryAdapter;

    @Autowired
    private WebhookFactory webhookFactory;

    private ObjectMapper objectMapper;

    @Autowired
    private PurchaseOrderAdapter purchaseOrderAdapter;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private UnitService unitService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @PostConstruct
    private void init() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    @Override
    public void onReceive(Webhook webhook) {
        switch (webhook.getType()) {
            case STOCK_API_SAVE_ENTRY:
                onReceiveSavePurchaseOrderByEntry(webhook);
                break;
        }
    }

    public void onSaveProduct(Product product) {
        try {
            send(webhookFactory.create(product), WebhookServer.STOCK_API, false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onSavePurchaseOrder(PurchaseOrder purchaseOrder) {
        try {
            send(webhookFactory.create(purchaseOrder), WebhookServer.STOCK_API, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Send web hook notification to save units.
     * Considerations: Units build a graph. Unit has units children and so on.
     * To save units its necessary to start from leafs and then go to root. This is the job of the
     * "associatedUnits" array. This function below basically walk in graph saving the right order and then
     * send units to web hook in this order
     *
     * @param unit
     */
    public void onSaveUnit(Unit unit, boolean wait) {
        try {
            send(webhookFactory.create(unit), WebhookServer.STOCK_API, wait);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void onSaveUnit(Unit unit) {
        onSaveUnit(unit, false);
    }


    public void onSaveCategory(Category category, boolean wait) {
        try {
            send(webhookFactory.create(category), WebhookServer.STOCK_API, wait);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onSaveCategory(Category category) {
        onSaveCategory(category, false);
    }

    @SuppressWarnings("unchecked")
    public void onReceiveSavePurchaseOrderByEntry(Webhook webhook) {
        Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>) objectMapper.convertValue(webhook.getData(), HashMap.class);
        purchaseOrderService.updateStatusByEntry(purchaseOrderAdapter.transform(map.get("order")));
    }

    public void emitWebhook(WebhookType type, Long id) {
        switch (type) {
            case PURCHASE_API_SAVE_PRODUCT:
                if (id != null) {
                    this.onSaveProduct(this.productService.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado")));
                } else {
                    this.productService.findAll().forEach(this::onSaveProduct);
                }
                break;

            case PURCHASE_API_SAVE_UNIT:
                if (id != null) {
                    this.onSaveUnit(this.unitService.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada")));
                } else {
                    this.unitService.findAll().forEach(this::onSaveUnit);
                }
                break;

            case PURCHASE_API_SAVE_CATEGORY:
                if (id != null) {
                    this.onSaveCategory(this.categoryService.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada")));
                } else {
                    this.categoryService.findAll().forEach(this::onSaveCategory);
                }
                break;

            case PURCHASE_API_SAVE_PURCHASE_ORDER:
                if (id != null) {
                    this.onSavePurchaseOrder(this.purchaseOrderService.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("Ordem de compra não encontrada")));
                } else {
                    this.purchaseOrderService.findAll().forEach(this::onSavePurchaseOrder);
                }
                break;

        }

    }
}
