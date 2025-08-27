package com.project.mog.service.shop;

import com.project.mog.dto.shop.ProductDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    
    // 실제로는 데이터베이스에서 가져올 예정
    private final List<ProductDto> products = List.of(
        ProductDto.builder()
                .id(1L)
                .name("프리미엄 요가매트")
                .category("equipment")
                .price(45000)
                .originalPrice(60000)
                .image("/img/yoga.jpeg")
                .badge("BEST")
                .description("고급 실리콘 소재의 안전한 요가매트")
                .detailedDescription("프리미엄 요가매트는 고급 실리콘 소재로 제작되어 미끄러움을 방지하고 안전한 요가 연습을 도와줍니다. 두께 6mm로 충분한 쿠션감을 제공하며, 다양한 요가 자세를 편안하게 수행할 수 있습니다.")
                .build(),
        ProductDto.builder()
                .id(2L)
                .name("스마트 웨이트")
                .category("equipment")
                .price(120000)
                .originalPrice(150000)
                .image("/img/abs.jpeg")
                .badge("NEW")
                .description("블루투스 연결 가능한 스마트 웨이트")
                .detailedDescription("스마트 웨이트는 블루투스 연결을 통해 운동 데이터를 실시간으로 추적하고 분석할 수 있는 혁신적인 운동 기구입니다. 앱과 연동하여 개인 맞춤형 운동 프로그램을 제공합니다.")
                .build(),
        ProductDto.builder()
                .id(3L)
                .name("운동복 세트")
                .category("clothing")
                .price(89000)
                .originalPrice(120000)
                .image("/img/Running.jpeg")
                .badge("SALE")
                .description("편안하고 스타일리시한 운동복")
                .detailedDescription("편안함과 스타일을 모두 만족시키는 프리미엄 운동복 세트입니다. 땀 흡수와 통기성이 뛰어난 소재로 제작되어 장시간 운동에도 편안하게 착용할 수 있습니다.")
                .build(),
        ProductDto.builder()
                .id(4L)
                .name("프로틴 파우더")
                .category("supplement")
                .price(65000)
                .originalPrice(80000)
                .image("/img/pushups.jpeg")
                .badge("HOT")
                .description("고품질 단백질 보충제")
                .detailedDescription("고품질 유청 단백질을 함유한 프로틴 파우더입니다. 근육 회복과 성장을 촉진하며, 다양한 맛으로 즐겁게 섭취할 수 있습니다.")
                .build(),
        ProductDto.builder()
                .id(5L)
                .name("테스트 상품")
                .category("equipment")
                .price(100)
                .originalPrice(1000)
                .image("/img/yoga.jpeg")
                .badge("TEST")
                .description("100원 테스트용 상품입니다 (아임포트 최소 금액)")
                .detailedDescription("결제 테스트를 위한 테스트 상품입니다. 아임포트의 최소 결제 금액인 100원으로 설정되어 있습니다.")
                .build()
    );
    
    public List<ProductDto> getAllProducts() {
        return products;
    }
    
    public ProductDto getProductById(Long id) {
        return products.stream()
                .filter(product -> product.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    public List<ProductDto> getProductsByCategory(String category) {
        if ("all".equals(category)) {
            return getAllProducts();
        }
        return products.stream()
                .filter(product -> product.getCategory().equals(category))
                .collect(Collectors.toList());
    }
}
